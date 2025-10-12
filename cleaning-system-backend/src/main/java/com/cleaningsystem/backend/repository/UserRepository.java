package com.cleaningsystem.backend.repository;

import com.cleaningsystem.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据用户名和密码查找用户（用于登录验证）
     */
    Optional<User> findByUsernameAndPassword(String username, String password);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据角色查找用户列表
     */
    List<User> findByRole(String role);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 自定义查询：根据用户名或邮箱查找用户
     */
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);
    
    /**
     * Find users by role list
     */
    List<User> findByRoleIn(List<String> roles);
    
    /**
     * Count users by role
     */
    long countByRole(String role);
    
    /**
     * Count users by multiple roles
     */
    long countByRoleIn(List<String> roles);
    
    /**
     * Find top users by roles ordered by creation date
     */
    List<User> findTop5ByRoleInOrderByCreatedAtDesc(List<String> roles);
    
    /**
     * Find top 2 users by roles ordered by creation date (for recent activities)
     */
    List<User> findTop2ByRoleInOrderByCreatedAtDesc(List<String> roles);
}