package com.stroke.clinical.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stroke.clinical.websocket.AlertWebSocketHandler;
import com.stroke.domain.event.BaseEvent;
import com.stroke.domain.event.CtTimeoutWarningEvent;
import com.stroke.domain.event.DntTimeoutWarningEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 超时事件处理器 — WebSocket 实时推送告警
 * <p>
 * 监听超时事件，通过 WebSocket 广播到所有前端。
 * 生产阶段扩展：RocketMQ 分发 + 站内信持久化。
 */
@Component
public class TimeoutEventHandler {

    private static final Logger log = LoggerFactory.getLogger(TimeoutEventHandler.class);

    private final InMemoryEventPublisher eventPublisher;
    private final AlertWebSocketHandler webSocketHandler;
    private final ObjectMapper mapper;

    public TimeoutEventHandler(InMemoryEventPublisher eventPublisher,
                               AlertWebSocketHandler webSocketHandler) {
        this.eventPublisher = eventPublisher;
        this.webSocketHandler = webSocketHandler;
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void registerListeners() {
        eventPublisher.registerListener("DNT_TIMEOUT_WARNING", this::handleDntTimeout);
        eventPublisher.registerListener("CT_TIMEOUT_WARNING", this::handleCtTimeout);
        eventPublisher.registerGlobalListener(this::logAllEvents);
        log.info("超时事件处理器已注册（WebSocket推送）");
    }

    private void handleDntTimeout(BaseEvent event) {
        DntTimeoutWarningEvent dntEvent = (DntTimeoutWarningEvent) event;
        String json = buildAlert("DNT_TIMEOUT", "🚨 DNT超时红灯",
                String.format("就诊 %d 的 DNT 已达 %d 分钟（阈值 %d 分钟）",
                        dntEvent.getEncounterId(), dntEvent.getDntMinutes(), dntEvent.getThreshold()),
                dntEvent.getEncounterId(), null);
        webSocketHandler.broadcast(json);
        log.warn("[DNT超时推送] encounterId={}, DNT={}min", dntEvent.getEncounterId(), dntEvent.getDntMinutes());
    }

    private void handleCtTimeout(BaseEvent event) {
        CtTimeoutWarningEvent ctEvent = (CtTimeoutWarningEvent) event;
        String json = buildAlert("CT_TIMEOUT", "💛 CT超时黄灯",
                String.format("就诊 %d 的 Door-to-CT 已达 %d 分钟（阈值 %d 分钟）",
                        ctEvent.getEncounterId(), ctEvent.getCtMinutes(), ctEvent.getThreshold()),
                ctEvent.getEncounterId(), null);
        webSocketHandler.broadcast(json);
        log.warn("[CT超时推送] encounterId={}, Door-to-CT={}min", ctEvent.getEncounterId(), ctEvent.getCtMinutes());
    }

    private void logAllEvents(BaseEvent event) {
        log.debug("[事件日志] type={}, encounterId={}", event.getEventType(), event.getEncounterId());
    }

    private String buildAlert(String type, String title, String message,
                              Long encounterId, Long patientId) {
        try {
            Map<String, Object> alert = new LinkedHashMap<>();
            alert.put("type", type);
            alert.put("title", title);
            alert.put("message", message);
            alert.put("encounterId", encounterId);
            alert.put("patientId", patientId);
            alert.put("severity", "DNT_TIMEOUT".equals(type) ? "critical" : "warning");
            return mapper.writeValueAsString(alert);
        } catch (JsonProcessingException e) {
            log.error("构建告警JSON失败", e);
            return "{}";
        }
    }
}
