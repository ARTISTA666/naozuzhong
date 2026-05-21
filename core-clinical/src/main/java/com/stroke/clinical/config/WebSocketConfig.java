package com.stroke.clinical.config;

import com.stroke.clinical.websocket.AlertWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置 — 实时推送告警
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    /** 告警推送端点 */
    public static final String ALERT_ENDPOINT = "/ws/alerts";

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(alertHandler(), ALERT_ENDPOINT)
                .setAllowedOrigins("*");
    }

    @Bean
    public AlertWebSocketHandler alertHandler() {
        return new AlertWebSocketHandler();
    }
}
