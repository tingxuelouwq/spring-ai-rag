package com.kevin.springai.rag.controller;

import com.kevin.springai.rag.common.BaseResponse;
import com.kevin.springai.rag.common.ResultUtils;
import com.kevin.springai.rag.constant.BizConstant;
import com.kevin.springai.rag.dto.QueryFileDTO;
import com.kevin.springai.rag.entity.AliOssFile;
import com.kevin.springai.rag.enums.ChunkStrategy;
import com.kevin.springai.rag.enums.ErrorCode;
import com.kevin.springai.rag.service.AliOssFileService;
import com.kevin.springai.rag.vo.ChunkPreviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 知识库管理接口
 *
 * @author Xushu
 */
@Tag(name = "KnowledgeController", description = "知识库管理接口")
@Slf4j
@RestController
@RequestMapping(BizConstant.API_VERSION + "/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final AliOssFileService aliOssFileService;

    /**
     * 上传附件
     * <p>
     * 完成文件上传 OSS、按指定策略分片、向量化、持久化数据库的完整流程。
     * </p>
     *
     * @param files    上传的文件列表
     * @param strategy 分片策略，默认 token
     * @return 操作结果
     */
    @Operation(summary = "upload", description = "上传附件接口")
    @PostMapping(value = "file/upload", headers = "content-type=multipart/form-data")
    public BaseResponse<Void> upload(
            @RequestParam("file") List<MultipartFile> files,
            @RequestParam(value = "strategy", defaultValue = "token") String strategy) {
        if (files == null || files.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请上传文件");
        }
        aliOssFileService.uploadFiles(files, ChunkStrategy.fromCode(strategy));
        return ResultUtils.success(null, "文件上传成功");
    }

    /**
     * 分页查询文件
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @Operation(summary = "contents", description = "文件查询")
    @GetMapping("/contents")
    public BaseResponse<Page<AliOssFile>> queryFiles(@Validated QueryFileDTO request) {
        return aliOssFileService.queryPage(request);
    }

    /**
     * 单个和批量删除文件
     *
     * @param ids 文件 ID 列表
     * @return 操作结果
     */
    @Operation(summary = "delete", description = "文件删除")
    @DeleteMapping("/delete")
    public BaseResponse<Void> deleteFiles(@RequestParam("ids") List<Long> ids) {
        return aliOssFileService.deleteFiles(ids);
    }

    /**
     * 下载单个文件
     * <p>
     * 重定向到 OSS 签名 URL，由浏览器直接下载，文件流不经过后端。
     * </p>
     *
     * @param id       文件 ID
     * @param response HTTP 响应
     */
    @Operation(summary = "download", description = "下载单个文件")
    @GetMapping("/download/{id}")
    public void downloadFile(@PathVariable("id") Long id, HttpServletResponse response) throws IOException {
        String signedUrl = aliOssFileService.getDownloadUrl(id);
        response.sendRedirect(signedUrl);
    }

    /**
     * 批量下载文件（打包为 ZIP）
     *
     * @param ids      文件 ID 列表
     * @param response HTTP 响应
     */
    @Operation(summary = "downloadBatch", description = "批量下载文件（打包为 ZIP）")
    @PostMapping("/download/batch")
    public void downloadBatch(@RequestBody List<Long> ids, HttpServletResponse response) {
        aliOssFileService.downloadAsZip(ids, response);
    }

    /**
     * 预览单个文件
     * <p>
     * 重定向到 OSS 签名 URL，浏览器内联打开。
     * </p>
     *
     * @param id       文件 ID
     * @param response HTTP 响应
     */
    @Operation(summary = "preview", description = "预览单个文件")
    @GetMapping("/preview/{id}")
    public void previewFile(@PathVariable("id") Long id, HttpServletResponse response) throws IOException {
        String signedUrl = aliOssFileService.getPreviewUrl(id);
        response.sendRedirect(signedUrl);
    }

    /**
     * 分片预览
     * <p>
     * 上传文件后，按指定策略分片，返回分片结果供用户预览，不写入向量库。
     * </p>
     *
     * @param file     上传的文件
     * @param strategy 分片策略，默认 token
     * @return 分片预览列表
     */
    @Operation(summary = "chunkPreview", description = "分片预览")
    @PostMapping(value = "/chunk/preview", headers = "content-type=multipart/form-data")
    public BaseResponse<List<ChunkPreviewVO>> chunkPreview(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "strategy", defaultValue = "token") String strategy) {
        if (file == null || file.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请上传文件");
        }
        List<ChunkPreviewVO> preview = aliOssFileService.previewChunks(file, ChunkStrategy.fromCode(strategy));
        return ResultUtils.success(preview);
    }
}