package com.xushu.rag.controller;

import com.kevin.springai.rag.common.BaseResponse;
import com.kevin.springai.rag.common.ResultUtils;
import com.kevin.springai.rag.constant.BizConstant;
import com.kevin.springai.rag.dto.LogInfoPageQueryDTO;
import com.kevin.springai.rag.entity.LogInfo;
import com.kevin.springai.rag.service.LogInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志控制器
 */
@Tag(name = "LogInfoController", description = "日志控制器")
@Slf4j
@RestController
@RequestMapping(BizConstant.API_VERSION + "/log")
@RequiredArgsConstructor
public class LogInfoController {

    private final LogInfoService logInfoService;

    /**
     * 分页查询日志信息（带条件查询）
     *
     * @param queryDTO 分页查询条件
     * @return 日志分页结果
     */
    @Operation(summary = "分页查询日志信息（带条件查询）")
    @GetMapping("/page")
    public BaseResponse<Page<LogInfo>> getLogInfoPage(@Validated LogInfoPageQueryDTO queryDTO) {
        log.info("分页查询日志信息，参数：{}", queryDTO);
        Page<LogInfo> result = logInfoService.pageQuery(queryDTO);
        return ResultUtils.success(result);
    }

    /**
     * 清空全部日志
     *
     * @return 操作结果
     */
    @Operation(summary = "清空全部日志")
    @PostMapping("/clear")
    public BaseResponse<Void> clearLogInfos() {
        log.info("清空全部日志");
        logInfoService.removeAll();
        return ResultUtils.success(null, "删除成功");
    }
}