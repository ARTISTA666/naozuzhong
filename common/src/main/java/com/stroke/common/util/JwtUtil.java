package com.stroke.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 令牌工具类
 * <p>
 * 生成/验证/解析 Token，包含用户ID、账号、角色声明。
 * 密钥可从环境变量获取，默认使用开发密钥。
 * </p>
 */
public class JwtUtil {

    /** Token 有效期：24 小时 */
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000L;

    /** 密钥（生产环境应从环境变量 JWT_SECRET 读取） */
    private static final String SECRET_KEY;

    static {
        String env = System.getenv("JWT_SECRET");
        SECRET_KEY = (env != null && !env.isBlank()) ? env : "stroke-dev-jwt-secret-key-2026-must-be-256-bit";
    }

    private static SecretKey signingKey() {
        byte[] bytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            // 补足 256 位
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, Math.min(bytes.length, 32));
            return Keys.hmacShaKeyFor(padded);
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    /** 生成 Token */
    public static String generate(Long userId, String username, List<String> roles) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRATION_MS))
                .signWith(signingKey())
                .compact();
    }

    /** 解析 Token，返回 Claims */
    public static Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 验证 Token 是否有效 */
    public static boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 从 Token 提取用户 ID */
    public static Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    /** 从 Token 提取用户名 */
    public static String getUsername(String token) {
        return parse(token).get("username", String.class);
    }

    /** 从 Token 提取角色列表 */
    @SuppressWarnings("unchecked")
    public static List<String> getRoles(String token) {
        return parse(token).get("roles", List.class);
    }

    /** 获取过期时间 */
    public static Date getExpiration(String token) {
        return parse(token).getExpiration();
    }

    /** 判断 Token 是否已过期 */
    public static boolean isExpired(String token) {
        return getExpiration(token).before(new Date());
    }
}
