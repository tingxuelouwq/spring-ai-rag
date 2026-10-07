package com.kevin.springai.rag.service.impl;

import com.kevin.springai.rag.enums.ChunkStrategy;
import com.kevin.springai.rag.service.DocumentChunkService;
import com.kevin.springai.rag.vo.ChunkPreviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentChunkServiceImpl implements DocumentChunkService {

    /** 递归拆分默认阈值 */
    private static final int DEFAULT_MAX_CHUNK_SIZE = 800;

    /** 结构分块不降级，允许超长 */
    private static final int STRUCTURE_MAX_CHUNK_SIZE = 5000;

    @Override
    public List<Document> split(List<Document> documents, ChunkStrategy strategy) {
        log.info("分片策略: {}", strategy.getCode());
        return switch (strategy) {
            case TOKEN -> TokenTextSplitter.builder().build().apply(documents);
            case PARAGRAPH -> splitByParagraph(documents);
            case SENTENCE -> splitBySentence(documents);
            case RECURSIVE -> splitRecursive(documents);
            case STRUCTURE -> splitByStructure(documents);
        };
    }

    @Override
    public List<ChunkPreviewVO> preview(List<Document> documents, ChunkStrategy strategy) {
        List<Document> chunks = split(documents, strategy);
        List<ChunkPreviewVO> result = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            Document chunk = chunks.get(i);
            if (StringUtils.hasLength(chunk.getText())) {
                result.add(ChunkPreviewVO.builder()
                        .index(i + 1)
                        .content(chunk.getText())
                        .length(chunk.getText().length())
                        .metadata(chunk.getMetadata())
                        .build());
            }
        }
        return result;
    }

    /** 按段落切分（空行分隔） */
    private List<Document> splitByParagraph(List<Document> documents) {
        List<Document> result = new ArrayList<>();
        for (Document doc : documents) {
            String[] paragraphs;
            if (StringUtils.hasLength(doc.getText())) {
                paragraphs = doc.getText().split("\\n\\s*\\n");
                for (String p : paragraphs) {
                    if (!p.isBlank()) {
                        result.add(new Document(p.trim(), doc.getMetadata()));
                    }
                }
            }
        }
        return result;
    }

    /** 按句子切分（中英文句号、问号、感叹号） */
    private List<Document> splitBySentence(List<Document> documents) {
        List<Document> result = new ArrayList<>();
        for (Document doc : documents) {
            String[] sentences;
            if (StringUtils.hasLength(doc.getText())) {
                sentences = doc.getText().split("(?<=[。！？.!?])\\s*");
                for (String s : sentences) {
                    if (!s.isBlank()) {
                        result.add(new Document(s.trim(), doc.getMetadata()));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 递归拆分
     * <p>
     * 策略：先按段落切 → 超长段落再按句子切 → 仍超长再按字符硬切兜底。
     * 保证每个分片不超过 {@link #DEFAULT_MAX_CHUNK_SIZE} 字符。
     * </p>
     */
    private List<Document> splitRecursive(List<Document> documents) {
        List<Document> result = new ArrayList<>();
        for (Document doc : documents) {
            recursiveSplit(doc.getText(), doc.getMetadata(), result);
        }
        return result;
    }

    /**
     * 递归拆分单个文本
     *
     * @param text     待拆分的文本
     * @param metadata 元数据
     * @param result   结果收集器
     */
    private void recursiveSplit(String text, Map<String, Object> metadata, List<Document> result) {
        if (text == null || text.isBlank()) {
            return;
        }

        // 1. 未超长，直接作为一个分片
        if (text.length() <= DEFAULT_MAX_CHUNK_SIZE) {
            result.add(new Document(text.trim(), metadata));
            return;
        }

        // 2. 超长，尝试按段落切
        String[] paragraphs = text.split("\\n\\s*\\n");
        if (paragraphs.length > 1) {
            for (String p : paragraphs) {
                recursiveSplit(p, metadata, result);
            }
            return;
        }

        // 3. 单段仍超长，尝试按句子切
        String[] sentences = text.split("(?<=[。！？.!?])\\s*");
        if (sentences.length > 1) {
            for (String s : sentences) {
                recursiveSplit(s, metadata, result);
            }
            return;
        }

        // 4. 句子仍超长（或没有句子边界），按固定长度硬切兜底
        for (int i = 0; i < text.length(); i += DEFAULT_MAX_CHUNK_SIZE) {
            int end = Math.min(i + DEFAULT_MAX_CHUNK_SIZE, text.length());
            result.add(new Document(text.substring(i, end).trim(), metadata));
        }
    }

    /**
     * 基于文档结构分块
     * <p>
     * 识别 Markdown 标题（# ## ###）作为分块边界，
     * 每个标题及其下属内容作为一个分块。
     * 代码块（```）、表格等结构不会被从中间切断。
     * </p>
     */
    private List<Document> splitByStructure(List<Document> documents) {
        List<Document> result = new ArrayList<>();
        for (Document doc : documents) {
            result.addAll(splitByStructure(doc.getText(), doc.getMetadata()));
        }
        return result;
    }

    /**
     * 按 Markdown 结构切分单个文本
     *
     * @param text     文本内容
     * @param metadata 元数据
     * @return 分块结果
     */
    private List<Document> splitByStructure(String text, Map<String, Object> metadata) {
        List<Document> result = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return result;
        }

        String[] lines = text.split("\\r?\\n");
        StringBuilder currentChunk = new StringBuilder();
        String currentTitle = "";
        boolean inCodeBlock = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                currentChunk.append(line).append("\n");
                continue;
            }

            if (inCodeBlock) {
                currentChunk.append(line).append("\n");
                continue;
            }

            boolean isHeading = trimmed.matches("^#{1,6}\\s+.*");
            log.debug("行: [{}] 是否标题: {}", trimmed, isHeading);

            if (isHeading && currentChunk.length() > 0) {
                appendChunk(result, currentChunk.toString(), currentTitle, metadata);
                currentChunk.setLength(0);
            }

            if (isHeading) {
                currentTitle = trimmed.replaceAll("^#+\\s+", "");
            }

            currentChunk.append(line).append("\n");
        }

        if (currentChunk.length() > 0) {
            appendChunk(result, currentChunk.toString(), currentTitle, metadata);
        }

        return result;
    }

    /**
     * 构建分块文档
     * <p>
     * 如果分块超过最大长度，降级用递归拆分继续切。
     * </p>
     *
     * @param content  分块内容
     * @param title    所属标题
     * @param metadata 元数据
     */
    private void appendChunk(List<Document> result, String content, String title,
                             Map<String, Object> metadata) {
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        log.info("准备构建分块: title={}, length={}, 是否降级={}",
                title, trimmed.length(), trimmed.length() > STRUCTURE_MAX_CHUNK_SIZE);

        if (trimmed.length() > STRUCTURE_MAX_CHUNK_SIZE) {
            // 超长，降级递归拆分
            List<Document> subChunks = new ArrayList<>();
            recursiveSplit(trimmed, metadata, subChunks);
            subChunks.forEach(c -> {
                if (!title.isEmpty()) {
                    c.getMetadata().put("title", title);
                }
            });
            result.addAll(subChunks);
        } else {
            Map<String, Object> chunkMetadata = new HashMap<>(metadata);
            if (!title.isEmpty()) {
                chunkMetadata.put("title", title);
            }
            result.add(new Document(trimmed, chunkMetadata));
        }
    }
}