package com.stroke.clinical.event;

import com.stroke.domain.event.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 内存事件发布器 — 开发阶段使用的轻量实现
 * <p>
 * 后续可无痛替换为 RocketMQ 生产端：
 * 1. 实现 EventPublisher 接口
 * 2. 在 publish() 中调用 rocketMQTemplate.send()
 * 3. 监听方改为 @RocketMQMessageListener
 */
@Component
public class InMemoryEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InMemoryEventPublisher.class);

    /** 事件类型 → 监听器列表 */
    private final Map<String, List<EventListener>> listeners = new java.util.concurrent.ConcurrentHashMap<>();

    /** 通配监听器（监听所有事件，用于日志/审计） */
    private final List<EventListener> globalListeners = new CopyOnWriteArrayList<>();

    @PostConstruct
    void init() {
        log.info("事件发布器初始化: InMemoryEventPublisher（开发模式）");
    }

    @Override
    public void publish(BaseEvent event) {
        log.debug("发布事件: type={}, eventId={}, encounterId={}",
                event.getEventType(), event.getEventId(), event.getEncounterId());

        // 通知类型匹配的监听器
        List<EventListener> typeListeners = listeners.get(event.getEventType());
        if (typeListeners != null) {
            for (EventListener listener : typeListeners) {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    log.error("事件监听器执行异常: type={}, error={}", event.getEventType(), e.getMessage(), e);
                }
            }
        }

        // 通知全局监听器
        for (EventListener listener : globalListeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.error("全局事件监听器异常: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void registerListener(String eventType, EventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(listener);
        log.debug("注册事件监听: type={}", eventType);
    }

    /**
     * 注册通配监听器（监听所有事件）
     */
    public void registerGlobalListener(EventListener listener) {
        globalListeners.add(listener);
    }
}
