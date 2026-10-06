package com.kevin.springai.rag.service;

import com.kevin.springai.rag.common.BaseResponse;
import com.kevin.springai.rag.dto.QueryFileDTO;
import com.kevin.springai.rag.entity.AliOssFile;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * OSS 文件服务
 */
public interface AliOssFileService {

    /**
     * 批量上传文件
     * <p>
     * 完成文件上传 OSS、文本向量化、持久化数据库的完整流程。
     * </p>
     *
     * @param files 上传的文件列表
     */
    void uploadFiles(List<MultipartFile> files);

    /**
     * 分页查询文件
     *
     * @param request 查询条件
     * @return 分页结果
     */
    BaseResponse<Page<AliOssFile>> queryPage(QueryFileDTO request);

    /**
     * 批量删除文件（同时删除向量与 OSS 对象）
     *
     * @param ids 文件 ID 列表
     * @return 操作结果
     */
    BaseResponse<Void> deleteFiles(List<Long> ids);

    /**
     * 批量下载文件
     *
     * @param ids 文件 ID 列表
     * @return 操作结果
     */
    BaseResponse<Void> downloadFiles(List<Long> ids);

    /**
     * 获取单个文件的下载地址（签名 URL）
     *
     * @param id 文件 ID
     * @return 签名 URL
     */
    String getDownloadUrl(Long id);
}