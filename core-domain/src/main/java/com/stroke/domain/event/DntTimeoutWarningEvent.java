package com.stroke.domain.event;

/**
 * DNT 超时告警事件 — 对应设计文档 5.2 节 dnt.timeout.warning
 * <p>
 * 触发条件：Door-to-Needle > 45 分钟
 * 处理：推送至主任/质控员站内信 + PAD推送
 */
public class DntTimeoutWarningEvent extends BaseEvent {

    /** DNT 实际分钟数 */
    private long dntMinutes;

    /** 阈值 */
    private long threshold;

    /** 当前绿道状态 */
    private String currentStatus;

    public DntTimeoutWarningEvent() {}

    public DntTimeoutWarningEvent(String eventId, Long encounterId, Long patientId,
                                   long dntMinutes, long threshold) {
        super(eventId, "DNT_TIMEOUT_WARNING", encounterId, patientId);
        this.dntMinutes = dntMinutes;
        this.threshold = threshold;
    }

    // ====== Getters & Setters ======

    public long getDntMinutes() { return dntMinutes; }
    public void setDntMinutes(long dntMinutes) { this.dntMinutes = dntMinutes; }

    public long getThreshold() { return threshold; }
    public void setThreshold(long threshold) { this.threshold = threshold; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
}
