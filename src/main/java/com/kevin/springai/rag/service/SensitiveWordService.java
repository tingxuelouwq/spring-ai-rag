package com.kevin.springai.rag.service;

import com.kevin.springai.rag.dto.SensitiveWordPageQueryDTO;
import com.kevin.springai.rag.entity.SensitiveWord;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 敏感词服务
 */
public interface SensitiveWordService {

    /** 新增敏感词 */
    void saveWord(SensitiveWord sensitiveWord);

    /** 删除敏感词 */
    void removeWord(Long id);

    /** 批量删除敏感词 */
    void removeWords(List<Long> ids);

    /** 更新敏感词 */
    void updateWord(SensitiveWord sensitiveWord);

    /** 分页查询敏感词 */
    Page<SensitiveWord> pageWords(SensitiveWordPageQueryDTO queryDTO);

    /** 查询所有敏感词 */
    List<SensitiveWord> listAll();

    /**
     * 启用/禁用敏感词
     */
    void startOrStop(String status, Long id);

    /**
     * 敏感词过滤
     */
    String findHitWord(String message);
}