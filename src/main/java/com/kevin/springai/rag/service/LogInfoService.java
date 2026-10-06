package com.kevin.springai.rag.service;

import com.kevin.springai.rag.dto.LogInfoPageQueryDTO;
import com.kevin.springai.rag.entity.LogInfo;
import org.springframework.data.domain.Page;

/**
 * 日志服务
 */
public interface LogInfoService {

    /**
     * 保存日志
     *
     * @param logInfo 日志实体
     */
    void save(LogInfo logInfo);

    /**
     * 分页查询日志
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<LogInfo> pageQuery(LogInfoPageQueryDTO queryDTO);

    /**
     * 清空全部日志
     */
    void removeAll();
}