package com.kevin.springai.rag.repository;

import com.kevin.springai.rag.entity.AliOssFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OSS 文件数据访问层（Spring Data JPA）
 */
@Repository
public interface AliOssFileRepository extends JpaRepository<AliOssFile, Integer> {

    /**
     * 按文件名模糊分页查询
     *
     * @param fileName 文件名关键字，可为 null
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT f FROM AliOssFile f WHERE (:fileName IS NULL OR f.fileName LIKE %:fileName%)")
    Page<AliOssFile> findByFileNameContaining(@Param("fileName") String fileName, Pageable pageable);

    /**
     * 按 ID 列表查询文件
     *
     * @param ids ID 列表
     * @return 文件列表
     */
    List<AliOssFile> findByIdIn(List<Integer> ids);

    /**
     * 按 ID 列表批量删除
     *
     * @param ids ID 列表
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM AliOssFile f WHERE f.id IN :ids")
    int deleteByIdIn(@Param("ids") List<Integer> ids);
}