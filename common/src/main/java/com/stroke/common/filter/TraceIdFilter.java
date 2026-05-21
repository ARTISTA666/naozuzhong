package com.stroke.common.filter;

import org.slf4j.MDC;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * MDC TraceId 过滤器
 * <p>
 * 每次请求自动生成 traceId，注入 MDC 和响应头，
 * 实现分布式请求链路追踪。
 * </p>
 */
public class TraceIdFilter implements Filter {

    /** 请求头/响应头名称 */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String MDC_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 优先复用上游的 traceId（网关透传），否则新生成
        String traceId = req.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        MDC.put(MDC_KEY, traceId);
        resp.setHeader(TRACE_ID_HEADER, traceId);
        req.setAttribute(MDC_KEY, traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
