package com.kevin.springai.rag.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 分片预览结果
 */
@Data
@Builder
public class ChunkPreviewVO {

    /** 分片序号 */
    private int index;

    /** 分片内容 */
    private String content;

    /** 分片字符数 */
    private int length;

    /** 元数据 */
    private Map<String, Object> metadata;
}