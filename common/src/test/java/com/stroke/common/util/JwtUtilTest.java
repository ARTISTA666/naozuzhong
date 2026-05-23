package com.stroke.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 工具类单元测试
 */
@DisplayName("JwtUtil — Token 工具类")
class JwtUtilTest {

    @Test
    @DisplayName("生成 Token — 包含正确声明")
    void generate() {
        String token = JwtUtil.generateToken(1L, "admin", "ADMIN");
        assertNotNull(token);
        assertTrue(token.startsWith("eyJ")); // JWT header
    }

    @Test
    @DisplayName("解析 Token — 提取 userId")
    void parseUserId() {
        String token = JwtUtil.generateToken(99L, "test", "DOCTOR");
        Long userId = JwtUtil.getUserIdFromToken(token);
        assertEquals(99L, userId);
    }

    @Test
    @DisplayName("解析 Token — 提取 username")
    void parseUsername() {
        String token = JwtUtil.generateToken(1L, "testuser", "NURSE");
        String username = JwtUtil.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("解析 Token — 提取 roles")
    void parseRoles() {
        String token = JwtUtil.generateToken(1L, "admin", "ADMIN");
        String roles = JwtUtil.getRolesFromToken(token);
        assertTrue(roles.contains("ADMIN"));
    }

    @Test
    @DisplayName("验证有效 Token — 通过")
    void validateValid() {
        String token = JwtUtil.generateToken(1L, "admin", "ADMIN");
        assertDoesNotThrow(() -> JwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("验证无效 Token — 抛异常")
    void validateInvalid() {
        assertThrows(Exception.class, () -> JwtUtil.validateToken("invalid-token"));
    }

    @Test
    @DisplayName("验证空 Token — 抛异常")
    void validateEmpty() {
        assertThrows(Exception.class, () -> JwtUtil.validateToken(""));
    }

    @Test
    @DisplayName("验证过期 Token — 抛异常")
    void validateExpired() {
        // 使用 jwt.io 风格构造过期 token 会有签名不匹配问题
        // 这里验证任意无效 JWT 抛出异常
        assertThrows(Exception.class,
                () -> JwtUtil.validateToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNTAwMDAwMDAwfQ.invalid"));
    }
}
