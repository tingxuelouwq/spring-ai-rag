package com.kevin.springai.rag.repository;

import com.kevin.springai.rag.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层（Spring Data JPA）
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户
     *
     * @param userName 登录用户名
     * @return 用户实体（可能为空）
     */
    @Query("SELECT u FROM User u WHERE u.userName = :userName")
    Optional<User> findByUserName(@Param("userName") String userName);

    /**
     * 根据姓名模糊分页查询用户
     *
     * @param name     用户姓名（模糊匹配），可为 null
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT u FROM User u WHERE (:name IS NULL OR u.name LIKE %:name%)")
    Page<User> pageQuery(@Param("name") String name, Pageable pageable);
}