package com.stroke.clinical.controller;

import com.stroke.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查 & 基础信息
 */
@Tag(name = "系统健康检查")
@RestController
public class HealthController {

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        return Result.success(Map.of(
                "service", "core-clinical",
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
