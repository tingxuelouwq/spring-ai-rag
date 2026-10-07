package com.kevin.springai.rag.service.impl;

import com.kevin.springai.rag.common.ErrorCode;
import com.kevin.springai.rag.dto.SensitiveWordPageQueryDTO;
import com.kevin.springai.rag.entity.SensitiveWord;
import com.kevin.springai.rag.exception.BusinessException;
import com.kevin.springai.rag.repository.SensitiveWordRepository;
import com.kevin.springai.rag.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 敏感词服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordRepository sensitiveWordRepository;

    @Override
    public void saveWord(SensitiveWord sensitiveWord) {
        sensitiveWord.setId(null);
        sensitiveWord.setStatus("1");
        LocalDateTime now = LocalDateTime.now();
        sensitiveWord.setCreatedAt(now);
        sensitiveWord.setUpdatedAt(now);
        sensitiveWordRepository.save(sensitiveWord);
    }

    @Override
    public void removeWord(Long id) {
        if (!sensitiveWordRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        sensitiveWordRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeWords(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        sensitiveWordRepository.deleteByIdIn(ids);
    }

    @Override
    public void updateWord(SensitiveWord sensitiveWord) {
        if (sensitiveWord.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        SensitiveWord existing = sensitiveWordRepository.findById(sensitiveWord.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));

        existing.setWord(sensitiveWord.getWord());
        existing.setStatus(sensitiveWord.getStatus());
        existing.setUpdatedAt(LocalDateTime.now());
        sensitiveWordRepository.save(existing);
    }

    @Override
    public Page<SensitiveWord> pageWords(SensitiveWordPageQueryDTO queryDTO) {
        Pageable pageable = PageRequest.of(
                queryDTO.getPage(),
                queryDTO.getSize(),
                Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id"))
        );
        return sensitiveWordRepository.findAll(pageable);
    }

    @Override
    public List<SensitiveWord> listAll() {
        return sensitiveWordRepository.findAll();
    }

    @Override
    public void startOrStop(String status, Long id) {
        // 1. 校验状态值合法性
        if (!"1".equals(status) && !"0".equals(status)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 查询敏感词
        SensitiveWord word = sensitiveWordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));

        // 3. 更新状态
        word.setStatus(status);
        word.setUpdatedAt(LocalDateTime.now());
        sensitiveWordRepository.save(word);

        log.info("敏感词状态更新成功，id={}，status={}", id, status);
    }

    @Override
    public String findHitWord(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        // 只查启用状态的敏感词
        List<SensitiveWord> words = sensitiveWordRepository.findByStatus("1");
        for (SensitiveWord word : words) {
            if (text.contains(word.getWord())) {
                return word.getWord();
            }
        }
        return null;
    }
}