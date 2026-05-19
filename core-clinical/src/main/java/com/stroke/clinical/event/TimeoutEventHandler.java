package com.stroke.clinical.event;

import com.stroke.domain.event.BaseEvent;
import com.stroke.domain.event.CtTimeoutWarningEvent;
import com.stroke.domain.event.DntTimeoutWarningEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 超时事件处理器 — 监听超时告警事件并触发通知
 * <p>
 * 开发阶段：直接记录日志并调用通知方法。
 * 生产阶段：通过 RocketMQ 分发至 infrastructure 服务处理。
 * <p>
 * 对应设计文档 2.3 节通知事件。
 */
@Component
public class TimeoutEventHandler {

    private static final Logger log = LoggerFactory.getLogger(TimeoutEventHandler.class);

    private final InMemoryEventPublisher eventPublisher;

    public TimeoutEventHandler(InMemoryEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void registerListeners() {
        // 注册 DNT 超时监听
        eventPublisher.registerListener("DNT_TIMEOUT_WARNING", this::handleDntTimeout);

        // 注册 CT 超时监听
        eventPublisher.registerListener("CT_TIMEOUT_WARNING", this::handleCtTimeout);

        // 注册全局日志监听
        eventPublisher.registerGlobalListener(this::logAllEvents);

        log.info("超时事件处理器已注册");
    }

    /**
     * 处理 DNT 超时（红灯）
     * <p>
     * 设计文档 3.2.3: Door-to-Needle > 45分钟 → 通知主任/质控员
     */
    private void handleDntTimeout(BaseEvent event) {
        DntTimeoutWarningEvent dntEvent = (DntTimeoutWarningEvent) event;
        log.warn("🚨 [DNT超时处理] encounterId={}, DNT={}min, 状态={}",
                dntEvent.getEncounterId(), dntEvent.getDntMinutes(), dntEvent.getCurrentStatus());

        // TODO: 通过RocketMQ发送至infrastructure服务
        // 当前只做日志记录和本地通知模拟
        notifyDntTimeout(dntEvent);
    }

    /**
     * 处理 CT 超时（黄灯）
     * <p>
     * 设计文档 3.2.3: Door-to-CT > 25分钟 → 工作站弹窗提醒
     */
    private void handleCtTimeout(BaseEvent event) {
        CtTimeoutWarningEvent ctEvent = (CtTimeoutWarningEvent) event;
        log.warn("💛 [CT超时处理] encounterId={}, Door-to-CT={}min",
                ctEvent.getEncounterId(), ctEvent.getCtMinutes());

        // TODO: 通过RocketMQ发送至infrastructure服务
        notifyCtTimeout(ctEvent);
    }

    /**
     * 所有事件的全局日志记录（用于审计追踪）
     */
    private void logAllEvents(BaseEvent event) {
        log.debug("[事件日志] type={}, eventId={}, encounterId={}, time={}",
                event.getEventType(), event.getEventId(),
                event.getEncounterId(), event.getTimestamp());
    }

    // ==================== 通知模拟（后续替换为真实推送） ====================

    private void notifyDntTimeout(DntTimeoutWarningEvent event) {
        log.warn("[DNT超时告警] encounterId={}, DNT={}分钟(阈值={}), 状态={}, 推送: 主任/质控员",
                event.getEncounterId(), event.getDntMinutes(),
                event.getThreshold(), event.getCurrentStatus());
    }

    private void notifyCtTimeout(CtTimeoutWarningEvent event) {
        log.warn("[CT超时提醒] encounterId={}, Door-to-CT={}分钟(阈值={}), 推送: 值班医生",
                event.getEncounterId(), event.getCtMinutes(), event.getThreshold());
    }
}
