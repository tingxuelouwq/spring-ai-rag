package com.kevin.springai.rag.service.impl;

import com.kevin.springai.rag.dto.LogInfoPageQueryDTO;
import com.kevin.springai.rag.entity.LogInfo;
import com.kevin.springai.rag.repository.LogInfoRepository;
import com.kevin.springai.rag.service.LogInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogInfoServiceImpl implements LogInfoService {

    private final LogInfoRepository logInfoRepository;

    @Override
    public void save(LogInfo logInfo) {
        logInfoRepository.save(logInfo);
    }

    @Override
    public Page<LogInfo> pageQuery(LogInfoPageQueryDTO queryDTO) {
        Pageable pageable = PageRequest.of(
                queryDTO.getPage(),
                queryDTO.getSize(),
                Sort.by(Sort.Order.desc("requestTime"), Sort.Order.desc("id"))
        );

        String methodName = StringUtils.hasText(queryDTO.getMethodName())
                ? queryDTO.getMethodName() : null;
        String className = StringUtils.hasText(queryDTO.getClassName())
                ? queryDTO.getClassName() : null;
        String requestParams = StringUtils.hasText(queryDTO.getRequestParams())
                ? queryDTO.getRequestParams() : null;

        return logInfoRepository.pageQuery(methodName, className, requestParams, pageable);
    }

    @Override
    @Transactional
    public void removeAll() {
        logInfoRepository.deleteAllLogs();
        log.info("已清空全部日志");
    }
}