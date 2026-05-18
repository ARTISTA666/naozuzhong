package com.stroke.clinical.event;

import com.stroke.domain.event.BaseEvent;

/**
 * 事件发布器接口 — 对应设计文档 2.3 节事件驱动架构
 * <p>
 * 职责：发布集成事件和通知事件，解耦核心服务与通知/外部系统。
 * <p>
 * 当前使用内存实现，后续可无痛替换为 RocketMQ 生产端。
 */
public interface EventPublisher {

    /**
     * 发布事件
     *
     * @param event 事件对象（集成事件或通知事件）
     */
    void publish(BaseEvent event);

    /**
     * 注册事件监听器
     */
    void registerListener(String eventType, EventListener listener);

    /**
     * 事件监听器接口
     */
    @FunctionalInterface
    interface EventListener {
        void onEvent(BaseEvent event);
    }
}
