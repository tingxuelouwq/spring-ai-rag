package com.kevin.springai.rag.service.impl;

import com.kevin.springai.rag.common.ErrorCode;
import com.kevin.springai.rag.dto.SensitiveCategoryPageQueryDTO;
import com.kevin.springai.rag.entity.SensitiveCategory;
import com.kevin.springai.rag.exception.BusinessException;
import com.kevin.springai.rag.repository.SensitiveCategoryRepository;
import com.kevin.springai.rag.service.SensitiveCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 敏感词分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveCategoryServiceImpl implements SensitiveCategoryService {

    private final SensitiveCategoryRepository sensitiveCategoryRepository;

    @Override
    public void saveCategory(SensitiveCategory entity) {
        entity.setId(null);
        entity.setStatus("1");
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedTime(now);
        entity.setUpdateTime(now);
        sensitiveCategoryRepository.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        sensitiveCategoryRepository.deleteByIdIn(ids);
    }

    @Override
    public void updateCategory(SensitiveCategory entity) {
        if (entity.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        SensitiveCategory existing = sensitiveCategoryRepository.findById(entity.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));

        existing.setCategoryName(entity.getCategoryName());
        existing.setStatus(entity.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        sensitiveCategoryRepository.save(existing);
    }

    @Override
    public Page<SensitiveCategory> pageCategories(SensitiveCategoryPageQueryDTO queryDTO) {
        Pageable pageable = PageRequest.of(
                queryDTO.getPage(),
                queryDTO.getSize(),
                Sort.by(Sort.Order.desc("updateTime"), Sort.Order.desc("id"))
        );
        return sensitiveCategoryRepository.findAll(pageable);
    }

    @Override
    public List<SensitiveCategory> listAll() {
        return sensitiveCategoryRepository.findAll();
    }

    @Override
    public void startOrStop(String status, Long id) {
        // 1. 校验状态值合法性
        if (!"1".equals(status) && !"0".equals(status)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 查询分类
        SensitiveCategory category = sensitiveCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));

        // 3. 更新状态
        category.setStatus(status);
        category.setUpdateTime(LocalDateTime.now());
        sensitiveCategoryRepository.save(category);

        log.info("敏感词分类状态更新成功，id={}，status={}", id, status);
    }
}