package com.stroke.domain.event;

import java.time.LocalDateTime;

/**
 * 事件基类 — 所有集成事件/通知事件的公共字段
 * <p>
 * 对应设计文档 2.3 节：所有集成事件携带 encounterId、patientId、timestamp、traceId。
 * 不强制使用 CloudEvents 等复杂封装。
 */
public abstract class BaseEvent {

    /** 事件ID（全局唯一） */
    private String eventId;

    /** 事件类型 */
    private String eventType;

    /** 事件发生时间 */
    private LocalDateTime timestamp;

    /** 链路追踪ID */
    private String traceId;

    /** 就诊ID */
    private Long encounterId;

    /** 患者ID */
    private Long patientId;

    public BaseEvent() {}

    public BaseEvent(String eventId, String eventType, Long encounterId, Long patientId) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.encounterId = encounterId;
        this.patientId = patientId;
        this.timestamp = LocalDateTime.now();
    }

    // ====== Getters & Setters ======

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public Long getEncounterId() { return encounterId; }
    public void setEncounterId(Long encounterId) { this.encounterId = encounterId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
}
