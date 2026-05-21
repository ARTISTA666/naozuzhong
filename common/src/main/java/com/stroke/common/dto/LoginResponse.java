package com.stroke.common.dto;

import java.util.List;

/** 登录响应 */
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String displayName;
    private String role;
    private List<String> permissions;

    public LoginResponse() {}

    public LoginResponse(String token, Long userId, String username, String displayName, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
