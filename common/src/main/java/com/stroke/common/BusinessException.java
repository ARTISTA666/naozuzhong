package com.stroke.common;

/**
 * 业务异常 — 用于在 Service 层抛出可预知的业务错误
 * <p>
 * 由 GlobalExceptionHandler 统一捕获并转换为 Result 返回。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public int getCode() {
        return code;
    }
}
