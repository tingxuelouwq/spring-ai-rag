package com.kevin.springai.rag.repository;

import com.kevin.springai.rag.entity.LogInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 日志数据访问层（Spring Data JPA）
 */
@Repository
public interface LogInfoRepository extends JpaRepository<LogInfo, Long> {

    /**
     * 多条件分页查询日志
     * <p>
     * 使用 JPQL 动态拼接条件，参数为 null 时该条件不生效。
     * </p>
     *
     * @param methodName    方法名（模糊匹配），可为 null
     * @param className     类名（模糊匹配），可为 null
     * @param requestParams 请求参数（模糊匹配），可为 null
     * @param pageable      分页参数
     * @return 分页结果
     */
    @Query("SELECT l FROM LogInfo l WHERE " +
            "(:methodName IS NULL OR l.methodName LIKE %:methodName%) AND " +
            "(:className IS NULL OR l.className LIKE %:className%) AND " +
            "(:requestParams IS NULL OR l.requestParams LIKE %:requestParams%)")
    Page<LogInfo> pageQuery(@Param("methodName") String methodName,
                            @Param("className") String className,
                            @Param("requestParams") String requestParams,
                            Pageable pageable);

    /**
     * 清空全部日志
     *
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM LogInfo")
    int deleteAllLogs();
}