package com.stroke.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Result 统一响应体测试
 */
@DisplayName("Result — 统一响应结构")
class ResultTest {

    @Test
    @DisplayName("success() — 200 + data")
    void success() {
        Result<String> res = Result.success("hello");
        assertEquals(200, res.getCode());
        assertEquals("操作成功", res.getMessage());
        assertEquals("hello", res.getData());
    }

    @Test
    @DisplayName("success() 自定义消息")
    void successCustom() {
        Result<String> res = Result.success("自定义消息", "data");
        assertEquals("自定义消息", res.getMessage());
        assertEquals("data", res.getData());
    }

    @Test
    @DisplayName("error() — 错误码 + 消息")
    void error() {
        Result<Void> res = Result.error(400, "参数错误");
        assertEquals(400, res.getCode());
        assertEquals("参数错误", res.getMessage());
        assertNull(res.getData());
    }

    @Test
    @DisplayName("badRequest() — 400")
    void badRequest() {
        Result<Void> res = Result.badRequest("缺少参数");
        assertEquals(400, res.getCode());
    }

    @Test
    @DisplayName("traceId — 链路追踪")
    void traceId() {
        Result<String> res = Result.success("ok").traceId("abc123");
        assertEquals("abc123", res.getTraceId());
    }
}
