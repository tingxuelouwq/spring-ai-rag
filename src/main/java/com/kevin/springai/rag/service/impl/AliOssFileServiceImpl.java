package com.kevin.springai.rag.service.impl;

import com.kevin.springai.rag.common.BaseResponse;
import com.kevin.springai.rag.common.ErrorCode;
import com.kevin.springai.rag.common.ResultUtils;
import com.kevin.springai.rag.dto.QueryFileDTO;
import com.kevin.springai.rag.entity.AliOssFile;
import com.kevin.springai.rag.repository.AliOssFileRepository;
import com.kevin.springai.rag.service.AliOssFileService;
import com.kevin.springai.rag.utils.AliOssUtil;
import com.kevin.springai.rag.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OSS 文件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AliOssFileServiceImpl implements AliOssFileService {

    private final AliOssFileRepository aliOssFileRepository;
    private final VectorStore vectorStore;
    private final AliOssUtil aliOssUtil;
    private final TokenTextSplitter tokenTextSplitter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFiles(List<MultipartFile> files) {
        for (MultipartFile file : files) {
            processSingleFile(file);
        }
    }

    /**
     * 处理单个文件：上传 OSS → 读取文本 → 分词 → 向量化 → 持久化
     * <p>
     * 任意步骤失败时，会逆序清理已产生的 OSS 文件和向量数据，避免孤岛资源。
     * </p>
     *
     * @param file 上传的文件
     */
    private void processSingleFile(MultipartFile file) {
        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new RuntimeException("文件名为空");
        }

        String objectName = null;
        List<String> vectorIds = null;

        try {
            // 1. 上传 OSS
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 随机文件名，避免同名但内容不同的文件覆盖
            objectName = UUID.randomUUID() + extension;
            String url = aliOssUtil.upload(file.getBytes(), objectName);

            // 2. 读取文件内容（支持 txt/pdf/docx/doc 等）
            Resource resource = file.getResource();
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            List<Document> documents = reader.read();

            // 3. 分词
            List<Document> splitDocuments = tokenTextSplitter.apply(documents);
            if (splitDocuments.isEmpty()) {
                throw new RuntimeException("文件内容为空，无法向量化：" + originalFilename);
            }

            // 4. 向量化并保存
            vectorStore.add(splitDocuments);
            vectorIds = splitDocuments.stream()
                    .map(Document::getId)
                    .collect(Collectors.toList());

            // 5. 持久化文件记录
            LocalDateTime now = LocalDateTime.now();
            AliOssFile aliOssFile = AliOssFile.builder()
                    .fileName(originalFilename)
                    .vectorId(JsonUtils.toJson(vectorIds))
                    .url(url)
                    .createTime(now)
                    .updateTime(now)
                    .build();
            aliOssFileRepository.save(aliOssFile);

            log.info("文件上传成功，fileName={}，vectorCount={}", originalFilename, vectorIds.size());
        } catch (Exception ex) {
            log.error("文件处理失败，开始清理已产生的资源，fileName={}", originalFilename, ex);
            // 逆序清理：先删向量，再删 OSS 文件
            cleanupVectors(vectorIds, originalFilename);
            cleanupOss(objectName, originalFilename);
            throw new RuntimeException("文件处理失败：" + originalFilename, ex);
        }
    }

    /**
     * 清理已写入的向量数据
     * <p>
     * 清理失败时只记录日志，不抛出异常，避免掩盖原始错误。
     * </p>
     *
     * @param vectorIds        已写入的向量 ID 列表，可为 null
     * @param originalFilename 原始文件名，用于日志
     */
    private void cleanupVectors(List<String> vectorIds, String originalFilename) {
        if (vectorIds == null || vectorIds.isEmpty()) {
            return;
        }
        try {
            vectorStore.delete(vectorIds);
            log.info("已回滚向量数据，fileName={}，count={}", originalFilename, vectorIds.size());
        } catch (Exception ex) {
            // 清理失败只能记日志，无法进一步处理
            log.error("回滚向量数据失败，fileName={}，vectorIds={}", originalFilename, vectorIds, ex);
        }
    }

    /**
     * 清理已上传的 OSS 文件
     * <p>
     * 清理失败时只记录日志，不抛出异常，避免掩盖原始错误。
     * </p>
     *
     * @param objectName       OSS 对象名，可为 null
     * @param originalFilename 原始文件名，用于日志
     */
    private void cleanupOss(String objectName, String originalFilename) {
        if (!StringUtils.hasText(objectName)) {
            return;
        }
        try {
            aliOssUtil.deleteObject(objectName);
            log.info("已回滚 OSS 文件，fileName={}，objectName={}", originalFilename, objectName);
        } catch (Exception ex) {
            log.error("回滚 OSS 文件失败，fileName={}，objectName={}", originalFilename, objectName, ex);
        }
    }

    @Override
    public BaseResponse<Page<AliOssFile>>  queryPage(QueryFileDTO request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getPageSize(),
                Sort.by(Sort.Order.desc("createTime"), Sort.Order.desc("id"))
        );

        String fileName = StringUtils.hasText(request.getFileName())
                ? request.getFileName() : null;

        Page<AliOssFile> page = aliOssFileRepository.findByFileNameContaining(fileName, pageable);

        // 将 url 替换为带签名的临时访问地址，下载时使用原始文件名
        page.getContent().forEach(file -> {
            String signedUrl = aliOssUtil.generatePresignedUrl(
                    file.getUrl(), file.getFileName(), 3600);
            file.setUrl(signedUrl);
        });

        return ResultUtils.success(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Void> deleteFiles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请选择文件");
        }

        List<AliOssFile> aliOssFiles = aliOssFileRepository.findByIdIn(ids);
        if (aliOssFiles.isEmpty()) {
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }

        int count = aliOssFileRepository.deleteByIdIn(ids);
        if (count == 0) {
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "删除失败");
        }

        for (AliOssFile file : aliOssFiles) {
            deleteVectors(file);
            aliOssUtil.deleteOss(file.getUrl());
        }

        log.info("批量删除文件成功，count={}", count);
        return ResultUtils.success(null, "成功删除 " + count + " 个文件");
    }

    /**
     * 删除文件关联的向量数据
     *
     * @param file 文件实体
     */
    private void deleteVectors(AliOssFile file) {
        String vectorId = file.getVectorId();
        if (!StringUtils.hasText(vectorId)) {
            return;
        }
        List<String> vectorIds = JsonUtils.parseList(vectorId, String.class);
        if (vectorIds.isEmpty()) {
            log.warn("向量 ID 解析结果为空，fileId={}, vectorId={}", file.getId(), vectorId);
            return;
        }
        vectorStore.delete(vectorIds);
    }

    @Override
    public BaseResponse<Void> downloadFiles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请选择文件");
        }

        List<AliOssFile> aliOssFiles = aliOssFileRepository.findByIdIn(ids);
        if (aliOssFiles.isEmpty()) {
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }

        for (AliOssFile file : aliOssFiles) {
            aliOssUtil.downloadByUrl(file.getUrl());
        }

        log.info("批量下载文件成功，count={}", aliOssFiles.size());
        return ResultUtils.success(null, "下载成功");
    }

    @Override
    public String getDownloadUrl(Long id) {
        AliOssFile file = aliOssFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));

        return aliOssUtil.generatePresignedUrl(
                file.getUrl(), file.getFileName(), 3600);
    }
}