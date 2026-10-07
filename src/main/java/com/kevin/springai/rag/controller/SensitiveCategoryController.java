package com.kevin.springai.rag.controller;

import com.kevin.springai.rag.common.BaseResponse;
import com.kevin.springai.rag.common.ResultUtils;
import com.kevin.springai.rag.constant.BizConstant;
import com.kevin.springai.rag.dto.SensitiveCategoryPageQueryDTO;
import com.kevin.springai.rag.entity.SensitiveCategory;
import com.kevin.springai.rag.service.SensitiveCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SensitiveCategoryController", description = "敏感词分类控制器")
@Slf4j
@RestController
@RequestMapping(BizConstant.API_VERSION + "/category")
@RequiredArgsConstructor
public class SensitiveCategoryController {

    private final SensitiveCategoryService sensitiveCategoryService;

    /**
     * 新增敏感词分类
     *
     * @param entity 分类信息
     * @return 操作结果
     */
    @Operation(summary = "新增敏感词分类")
    @PostMapping("/add")
    public BaseResponse<Void> create(@RequestBody @Validated SensitiveCategory entity) {
        log.info("新增敏感词分类：categoryName={}", entity.getCategoryName());
        sensitiveCategoryService.saveCategory(entity);
        return ResultUtils.success(null, "新增成功");
    }

    /**
     * 批量删除敏感词分类
     *
     * @param ids 分类 ID 列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("/batch")
    public BaseResponse<Void> batchDelete(@RequestBody List<Long> ids) {
        log.info("批量删除敏感词分类：count={}", ids == null ? 0 : ids.size());
        sensitiveCategoryService.removeCategories(ids);
        return ResultUtils.success(null, "删除成功");
    }

    /**
     * 修改敏感词分类
     *
     * @param entity 分类信息
     * @return 操作结果
     */
    @Operation(summary = "修改敏感词")
    @PutMapping("/update")
    public BaseResponse<Void> update(@RequestBody @Validated SensitiveCategory entity) {
        log.info("修改敏感词分类：id={}", entity.getId());
        sensitiveCategoryService.updateCategory(entity);
        return ResultUtils.success(null, "修改成功");
    }

    /**
     * 分页查询敏感词分类
     *
     * @param queryDTO 分页查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public BaseResponse<Page<SensitiveCategory>> page(
            @Validated SensitiveCategoryPageQueryDTO queryDTO) {
        return ResultUtils.success(sensitiveCategoryService.pageCategories(queryDTO));
    }

    /**
     * 获取全部敏感词分类
     *
     * @return 分类列表
     */
    @Operation(summary = "获取全部列表")
    @GetMapping("/list")
    public BaseResponse<List<SensitiveCategory>> list() {
        return ResultUtils.success(sensitiveCategoryService.listAll());
    }

    /**
     * 启用/禁用敏感词分类
     *
     * @param status 目标状态：1-启用，0-禁用
     * @param id     分类 ID
     * @return 操作结果
     */
    @Operation(summary = "启用禁用分类")
    @PostMapping("/status/{status}")
    public BaseResponse<Void> startOrStop(@PathVariable("status") String status,
                                          @RequestParam("id") Long id) {
        log.info("启用/禁用敏感词分类：id={}, status={}", id, status);
        sensitiveCategoryService.startOrStop(status, id);
        return ResultUtils.success(null, "1".equals(status) ? "启用成功" : "禁用成功");
    }
}