package com.stroke.common.filter;

import com.stroke.common.Result;
import com.stroke.common.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 共享 JWT 认证过滤器
 * <p>
 * 默认开启，infrastructure 服务通过 stroke.auth.shared-jwt-filter=false 禁用
 * （infrastructure 使用自己的 Spring Security JwtAuthFilter）。
 * <p>
 * 对所有非白名单路径进行 Token 校验。
 * 与 infrastructure 的 JwtAuthFilter 互补：本过滤器用于
 * clinical/rehab/analytics/integration 等无 Spring Security 的服务。
 * </p>
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "stroke.auth.shared-jwt-filter", havingValue = "true", matchIfMissing = true)
public class SharedJwtFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(SharedJwtFilter.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /** 白名单路径 — 不需要 Token */
    private static final List<String> WHITELIST = Arrays.asList(
            "/api/v3/auth/",         // 登录注册
            "/api/v3/health",        // 健康检查
            "/v3/api-docs",          // 接口文档
            "/swagger",              // Swagger
            "/ws/alerts",            // WebSocket
            "/greenway/test"         // 测试用
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        String path = req.getRequestURI();

        // 白名单放行
        if (isWhitelisted(path)) {
            chain.doFilter(req, resp);
            return;
        }

        // 检查 Authorization 头
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            sendUnauthorized(resp, "缺少认证Token");
            return;
        }

        // 验证 JWT
        String token = auth.substring(7);
        try {
            JwtUtil.validateToken(token);
        } catch (Exception e) {
            log.warn("JWT校验失败: {}", e.getMessage());
            sendUnauthorized(resp, "Token无效或已过期");
            return;
        }

        chain.doFilter(req, resp);
    }

    private boolean isWhitelisted(String path) {
        if (path == null) return false;
        for (String prefix : WHITELIST) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }

    private void sendUnauthorized(HttpServletResponse resp, String msg) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        String json = mapper.writeValueAsString(Result.error(401, msg));
        resp.getWriter().write(json);
    }
}
