package com.kevin.springai.rag.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档分片策略
 */
@Getter
@AllArgsConstructor
public enum ChunkStrategy {

    /** 按 token 数切分，默认策略 */
    TOKEN("token", "按Token切分"),

    /** 按段落切分 */
    PARAGRAPH("paragraph", "按段落切分"),

    /** 按句子切分 */
    SENTENCE("sentence", "按句子切分"),

    /** 递归拆分：先按段落，超长再按句子，仍超长再按字符兜底 */
    RECURSIVE("recursive", "递归拆分"),

    /** 基于文档结构：按 Markdown 标题层级切分，保留代码块和表格完整性 */
    STRUCTURE("structure", "基于文档结构"),

    ;

    private final String code;
    private final String desc;

    public static ChunkStrategy fromCode(String code) {
        for (ChunkStrategy s : values()) {
            if (s.code.equalsIgnoreCase(code)) {
                return s;
            }
        }
        return TOKEN;
    }
}