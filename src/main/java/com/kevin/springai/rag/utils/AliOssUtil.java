package com.kevin.springai.rag.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.model.GetObjectRequest;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 阿里云 OSS 工具类
 * <p>
 * 提供文件上传、删除、下载能力。内部复用单个 OSSClient 实例，
 * 由 Spring 容器管理生命周期，销毁时自动关闭。
 * </p>
 */
@Data
@Slf4j
public class AliOssUtil {

    /** 默认下载目录 */
    private static final String DEFAULT_DOWNLOAD_DIR =
            System.getProperty("java.io.tmpdir") + "/oss-download";

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /** 复用的 OSSClient 实例 */
    private OSS ossClient;

    public AliOssUtil(String endpoint, String accessKeyId, String accessKeySecret, String bucketName) {
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.bucketName = bucketName;
        this.ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * Spring 容器销毁时关闭 OSSClient，释放连接资源
     */
    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSSClient 已关闭");
        }
    }

    /**
     * 上传文件
     *
     * @param bytes      文件字节数组
     * @param objectName OSS 对象名（含路径，如 "docs/2026/file.pdf"）
     * @return 文件的公网访问地址
     */
    public String upload(byte[] bytes, String objectName) {
        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            log.error("OSS 上传失败：errorMessage={}, errorCode={}, requestId={}, hostId={}",
                    oe.getErrorMessage(), oe.getErrorCode(), oe.getRequestId(), oe.getHostId(), oe);
            throw oe;
        } catch (ClientException ce) {
            log.error("OSS 客户端异常：message={}", ce.getMessage(), ce);
            throw ce;
        }

        String url = "https://" + bucketName + "." + endpoint + "/" + objectName;
        log.info("文件上传成功，url={}", url);
        return url;
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件的完整访问地址
     * @return 删除成功返回 true
     */
    public boolean deleteOss(String fileUrl) {
        String objectName = extractObjectName(fileUrl);
        try {
            ossClient.deleteObject(bucketName, objectName);
            log.info("文件删除成功，objectName={}", objectName);
            return true;
        } catch (OSSException oe) {
            log.error("OSS 删除失败：errorMessage={}, errorCode={}, requestId={}",
                    oe.getErrorMessage(), oe.getErrorCode(), oe.getRequestId(), oe);
            throw oe;
        } catch (ClientException ce) {
            log.error("OSS 客户端异常：message={}", ce.getMessage(), ce);
            throw ce;
        }
    }

    /**
     * 下载文件到默认目录（系统临时目录下的 oss-download）
     *
     * @param objectName OSS 对象名（含路径）
     * @return 下载后的本地文件路径
     */
    public String download(String objectName) {
        return download(objectName, DEFAULT_DOWNLOAD_DIR);
    }

    /**
     * 下载文件到指定目录
     *
     * @param objectName OSS 对象名（含路径）
     * @param dirPath    本地保存目录，例如 "D:/fileOSS"
     * @return 下载后的本地文件路径
     */
    public String download(String objectName, String dirPath) {
        // 文件名中的 "/" 替换为 "_"，避免创建多级子目录
        String fileName = objectName.replace("/", "_");
        Path dir = Paths.get(dirPath);
        Path filePath = dir.resolve(fileName);

        try {
            Files.createDirectories(dir);
        } catch (Exception ex) {
            log.error("创建下载目录失败，dirPath={}", dirPath, ex);
            throw new RuntimeException("创建下载目录失败", ex);
        }

        try {
            ossClient.getObject(new GetObjectRequest(bucketName, objectName), filePath.toFile());
            log.info("文件下载成功，objectName={}, localPath={}", objectName, filePath);
            return filePath.toString();
        } catch (OSSException oe) {
            log.error("OSS 下载失败：errorMessage={}, errorCode={}, requestId={}",
                    oe.getErrorMessage(), oe.getErrorCode(), oe.getRequestId(), oe);
            throw oe;
        } catch (ClientException ce) {
            log.error("OSS 客户端异常：message={}", ce.getMessage(), ce);
            throw ce;
        }
    }

    /**
     * 从完整访问地址中提取 OSS 对象名
     *
     * @param fileUrl 文件访问地址
     * @return 对象名，解析失败时返回原字符串
     */
    private String extractObjectName(String fileUrl) {
        try {
            URL url = URI.create(fileUrl).toURL();
            return url.getPath().replaceFirst("/", "");
        } catch (MalformedURLException ex) {
            log.warn("文件地址解析失败，按原值作为 objectName 使用：{}", fileUrl);
            return fileUrl;
        }
    }
}