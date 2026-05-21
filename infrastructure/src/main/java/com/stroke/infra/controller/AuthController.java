package com.stroke.infra.controller;

import com.stroke.common.Result;
import com.stroke.common.dto.LoginRequest;
import com.stroke.common.dto.LoginResponse;
import com.stroke.common.dto.RegisterRequest;
import com.stroke.infra.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v3/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(authService.login(req));
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result<String> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return Result.success("注册成功");
    }
}
