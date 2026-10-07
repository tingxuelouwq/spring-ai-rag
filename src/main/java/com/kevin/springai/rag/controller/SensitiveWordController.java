package com.kevin.springai.rag.controller;

import com.kevin.springai.rag.common.BaseResponse;
import com.kevin.springai.rag.common.ResultUtils;
import com.kevin.springai.rag.constant.BizConstant;
import com.kevin.springai.rag.dto.SensitiveWordPageQueryDTO;
import com.kevin.springai.rag.entity.SensitiveWord;
import com.kevin.springai.rag.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SensitiveWordController", description = "敏感词控制器")
@Slf4j
@RestController
@RequestMapping(BizConstant.API_VERSION + "/sensitive")
@RequiredArgsConstructor
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    /**
     * 新增敏感词
     *
     * @param sensitiveWord 敏感词信息
     * @return 操作结果
     */
    @Operation(summary = "新增敏感词")
    @PostMapping("/add")
    public BaseResponse<Void> addSensitiveWord(@RequestBody @Validated SensitiveWord sensitiveWord) {
        log.info("新增敏感词：word={}", sensitiveWord.getWord());
        sensitiveWordService.saveWord(sensitiveWord);
        return ResultUtils.success(null, "新增成功");
    }

    /**
     * 批量删除敏感词
     *
     * @param ids 敏感词 ID 列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除敏感词")
    @DeleteMapping("/batch")
    public BaseResponse<Void> deleteSensitiveWords(@RequestBody List<Long> ids) {
        log.info("批量删除敏感词：count={}", ids == null ? 0 : ids.size());
        sensitiveWordService.removeWords(ids);
        return ResultUtils.success(null, "删除成功");
    }

    /**
     * 更新敏感词
     *
     * @param sensitiveWord 敏感词信息
     * @return 操作结果
     */
    @Operation(summary = "更新敏感词")
    @PutMapping
    public BaseResponse<Void> updateSensitiveWord(@RequestBody @Validated SensitiveWord sensitiveWord) {
        log.info("更新敏感词：id={}", sensitiveWord.getId());
        sensitiveWordService.updateWord(sensitiveWord);
        return ResultUtils.success(null, "更新成功");
    }

    /**
     * 分页查询敏感词
     *
     * @param queryDTO 分页查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询敏感词")
    @GetMapping("/page")
    public BaseResponse<Page<SensitiveWord>> getSensitiveWordPage(
            @Validated SensitiveWordPageQueryDTO queryDTO) {
        return ResultUtils.success(sensitiveWordService.pageWords(queryDTO));
    }

    /**
     * 查询所有敏感词
     *
     * @return 敏感词列表
     */
    @Operation(summary = "查询所有敏感词")
    @GetMapping
    public BaseResponse<List<SensitiveWord>> getAllSensitiveWords() {
        return ResultUtils.success(sensitiveWordService.listAll());
    }

    /**
     * 启用/禁用敏感词
     *
     * @param status 目标状态：1-启用，0-禁用
     * @param id     敏感词 ID
     * @return 操作结果
     */
    @Operation(summary = "启用禁用敏感词")
    @PostMapping("/status/{status}")
    public BaseResponse<Void> startOrStop(@PathVariable("status") String status,
                                          @RequestParam("id") Long id) {
        log.info("启用/禁用敏感词：id={}, status={}", id, status);
        sensitiveWordService.startOrStop(status, id);
        return ResultUtils.success(null, "1".equals(status) ? "启用成功" : "禁用成功");
    }
}