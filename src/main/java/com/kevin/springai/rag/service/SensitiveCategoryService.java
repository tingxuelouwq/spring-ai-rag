package com.kevin.springai.rag.service;

import com.kevin.springai.rag.dto.SensitiveCategoryPageQueryDTO;
import com.kevin.springai.rag.entity.SensitiveCategory;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 敏感词分类服务
 */
public interface SensitiveCategoryService {

    /** 新增分类 */
    void saveCategory(SensitiveCategory entity);

    /** 批量删除分类 */
    void removeCategories(List<Long> ids);

    /** 修改分类 */
    void updateCategory(SensitiveCategory entity);

    /** 分页查询分类 */
    Page<SensitiveCategory> pageCategories(SensitiveCategoryPageQueryDTO queryDTO);

    /** 查询全部分类 */
    List<SensitiveCategory> listAll();

    /**
     * 启用/禁用分类
     */
    void startOrStop(String status, Long id);
}