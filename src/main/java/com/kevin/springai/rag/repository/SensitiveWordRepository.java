package com.kevin.springai.rag.repository;

import com.kevin.springai.rag.entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 敏感词数据访问层（Spring Data JPA）
 */
@Repository
public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {

    /**
     * 按 ID 列表批量删除
     *
     * @param ids ID 列表
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM SensitiveWord s WHERE s.id IN :ids")
    int deleteByIdIn(@Param("ids") List<Long> ids);

    List<SensitiveWord> findByStatus(String status);
}