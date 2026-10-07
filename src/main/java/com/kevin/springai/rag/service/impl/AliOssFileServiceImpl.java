package com.kevin.springai.rag.service.impl;

import com.kevin.springai.rag.common.BaseResponse;
import com.kevin.springai.rag.common.ResultUtils;
import com.kevin.springai.rag.dto.QueryFileDTO;
import com.kevin.springai.rag.entity.AliOssFile;
import com.kevin.springai.rag.enums.ChunkStrategy;
import com.kevin.springai.rag.enums.ErrorCode;
import com.kevin.springai.rag.repository.AliOssFileRepository;
import com.kevin.springai.rag.service.AliOssFileService;
import com.kevin.springai.rag.service.DocumentChunkService;
import com.kevin.springai.rag.utils.AliOssUtil;
import com.kevin.springai.rag.utils.DateTimeUtil;
import com.kevin.springai.rag.utils.JsonUtils;
import com.kevin.springai.rag.vo.ChunkPreviewVO;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    @Transactional
    public void uploadFiles(List<MultipartFile> files, ChunkStrategy strategy) {
        for (MultipartFile file : files) {
            processSingleFile(file, strategy);
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
    private void processSingleFile(MultipartFile file, ChunkStrategy strategy) {
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

            // 3. 按指定策略分片
            List<Document> splitDocuments = documentChunkService.split(documents, strategy);
            if (splitDocuments.isEmpty()) {
                throw new RuntimeException("文件内容为空，无法向量化：" + originalFilename);
            }

            // 4. 给每个分片设置来源文件名
            for (Document doc : splitDocuments) {
                doc.getMetadata().put("source", originalFilename);
            }

            // 5. 向量化并保存
            vectorStore.add(splitDocuments);
            vectorIds = splitDocuments.stream()
                    .map(Document::getId)
                    .collect(Collectors.toList());

            // 6. 持久化文件记录
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
    public String getDownloadUrl(Long id) {
        AliOssFile file = aliOssFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));

        return aliOssUtil.generatePresignedUrl(
                file.getUrl(), file.getFileName(), 3600);
    }

    @Override
    public void downloadAsZip(List<Long> ids, HttpServletResponse response) {
        // 1. 参数校验
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("请选择文件");
        }

        List<AliOssFile> files = aliOssFileRepository.findByIdIn(ids);
        if (files.isEmpty()) {
            throw new RuntimeException("文件不存在");
        }

        // 2. 设置响应头
        String zipFileName = "files_" + DateTimeUtil.nowCompact() + ".zip";
        response.setContentType("application/zip");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + zipFileName + "\"");

        // 3. 边读 OSS 流边写入 ZIP
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            Set<String> usedNames = new HashSet<>();

            for (AliOssFile file : files) {
                String objectName = aliOssUtil.extractObjectName(file.getUrl());
                String entryName = uniqueName(file.getFileName(), usedNames);

                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);

                try (InputStream in = aliOssUtil.getObjectStream(objectName)) {
                    in.transferTo(zos);
                } catch (Exception ex) {
                    log.error("打包文件失败，objectName={}", objectName, ex);
                }

                zos.closeEntry();
            }
            zos.finish();
            log.info("ZIP 打包下载完成，文件数={}", files.size());
        } catch (IOException ex) {
            log.error("ZIP 打包失败", ex);
            throw new RuntimeException("文件下载失败", ex);
        }
    }

    @Override
    public String getPreviewUrl(Long id) {
        AliOssFile file = aliOssFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        return aliOssUtil.generatePreviewUrl(file.getUrl(), file.getFileName(), 3600);
    }

    /**
     * 生成不重复的文件名，避免 ZIP 内同名冲突
     *
     * @param fileName  原始文件名
     * @param usedNames 已使用的文件名集合
     * @return 唯一的文件名
     */
    private String uniqueName(String fileName, Set<String> usedNames) {
        if (!usedNames.contains(fileName)) {
            usedNames.add(fileName);
            return fileName;
        }
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
        String extension = dotIndex == -1 ? "" : fileName.substring(dotIndex);

        int index = 1;
        String newName;
        do {
            newName = baseName + "(" + index + ")" + extension;
            index++;
        } while (usedNames.contains(newName));

        usedNames.add(newName);
        return newName;
    }

    private final DocumentChunkService documentChunkService;

    @Override
    public List<ChunkPreviewVO> previewChunks(MultipartFile file, ChunkStrategy strategy) {
        try {
            Resource resource = file.getResource();
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            List<Document> documents = reader.read();
            return documentChunkService.preview(documents, strategy);
        } catch (Exception ex) {
            log.error("分片预览失败，fileName={}", file.getOriginalFilename(), ex);
            throw new RuntimeException("分片预览失败", ex);
        }
    }
}