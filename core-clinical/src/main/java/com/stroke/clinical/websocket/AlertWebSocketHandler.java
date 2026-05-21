package com.stroke.clinical.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 告警 WebSocket 处理器
 * <p>
 * 管理所有前端连接，当超时事件触发时广播通知。
 * 线程安全，支持并发连接。
 * </p>
 */
public class AlertWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(AlertWebSocketHandler.class);

    /** 活跃会话集合（线程安全） */
    private static final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WebSocket 连接建立: sessionId={}, 当前连接数={}", session.getId(), sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("WebSocket 连接关闭: sessionId={}, 原因={}, 剩余连接数={}",
                session.getId(), status.getReason(), sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 前端发来的心跳/订阅消息
        String payload = message.getPayload();
        if ("ping".equals(payload)) {
            send(session, "pong");
        }
        // 后续可扩展订阅特定 encounterId
    }

    /** 广播告警到所有前端 */
    public void broadcast(String alertJson) {
        TextMessage msg = new TextMessage(alertJson);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                send(session, msg);
            } else {
                sessions.remove(session);
            }
        }
    }

    private void send(WebSocketSession session, String text) {
        send(session, new TextMessage(text));
    }

    private void send(WebSocketSession session, TextMessage msg) {
        try {
            session.sendMessage(msg);
        } catch (IOException e) {
            log.warn("WebSocket 发送失败: sessionId={}", session.getId());
            sessions.remove(session);
        }
    }

    /** 获取当前连接数（监控用） */
    public int getActiveCount() {
        return sessions.size();
    }

    /** 心跳检查 & 清理断开的连接 */
    public void cleanStaleSessions() {
        sessions.removeIf(s -> !s.isOpen());
    }
}
