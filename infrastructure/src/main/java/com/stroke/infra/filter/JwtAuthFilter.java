package com.stroke.infra.filter;

import com.stroke.common.util.JwtUtil;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (JwtUtil.validate(token)) {
                    Long userId = JwtUtil.getUserId(token);
                    String username = JwtUtil.getUsername(token);
                    List<String> roles = JwtUtil.getRoles(token);

                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, username, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    // 传递用户信息到下游
                    request.setAttribute("X-User-Id", userId);
                    request.setAttribute("X-User-Name", username);
                    request.setAttribute("X-User-Roles", roles);
                }
            } catch (Exception ignored) {
                // Token 无效，继续保持未认证状态
            }
        }

        chain.doFilter(request, response);
    }
}
