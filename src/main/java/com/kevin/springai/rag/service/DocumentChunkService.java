package com.kevin.springai.rag.service;

import com.kevin.springai.rag.enums.ChunkStrategy;
import com.kevin.springai.rag.vo.ChunkPreviewVO;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 文档分片服务
 */
public interface DocumentChunkService {

    /**
     * 按指定策略分片
     *
     * @param documents 原始文档
     * @param strategy  分片策略
     * @return 分片后的文档列表
     */
    List<Document> split(List<Document> documents, ChunkStrategy strategy);

    /**
     * 分片并返回预览信息（不向量化）
     *
     * @param documents 原始文档
     * @param strategy  分片策略
     * @return 分片预览结果
     */
    List<ChunkPreviewVO> preview(List<Document> documents, ChunkStrategy strategy);
}