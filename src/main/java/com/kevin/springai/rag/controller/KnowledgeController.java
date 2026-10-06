package com.kevin.springai.rag.controller;

import com.kevin.springai.rag.common.BaseResponse;
import com.kevin.springai.rag.common.ErrorCode;
import com.kevin.springai.rag.common.ResultUtils;
import com.kevin.springai.rag.constant.BizConstant;
import com.kevin.springai.rag.dto.QueryFileDTO;
import com.kevin.springai.rag.entity.AliOssFile;
import com.kevin.springai.rag.service.AliOssFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * 完成文件上传 OSS、文本向量化、持久化数据库的完整流程。
     * </p>
     *
     * @param files 上传的文件列表
     * @return 操作结果
     */
    @Operation(summary = "upload", description = "上传附件接口")
    @PostMapping(value = "file/upload", headers = "content-type=multipart/form-data")
    public BaseResponse<Void> upload(@RequestParam("file") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请上传文件");
        }
        aliOssFileService.uploadFiles(files);
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
     * 批量删除文件
     *
     * @param ids 文件 ID 列表
     * @return 操作结果
     */
    @Operation(summary = "delete", description = "文件删除")
    @DeleteMapping("/delete")
    public BaseResponse<Void> deleteFiles(@RequestParam List<Integer> ids) {
        return aliOssFileService.deleteFiles(ids);
    }

    /**
     * 批量下载文件
     *
     * @param ids 文件 ID 列表
     * @return 操作结果
     */
    @Operation(summary = "download", description = "文件下载")
    @GetMapping("/download")
    public BaseResponse<Void> downloadFiles(@RequestParam List<Integer> ids) {
        return aliOssFileService.downloadFiles(ids);
    }
}