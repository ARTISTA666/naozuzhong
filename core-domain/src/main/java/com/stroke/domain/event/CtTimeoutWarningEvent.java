package com.stroke.domain.event;

/**
 * CT 超时告警事件 — 对应设计文档 3.2.3 节
 * <p>
 * 触发条件：Door-to-CT完成 > 25 分钟
 * 处理：工作站弹窗提醒
 */
public class CtTimeoutWarningEvent extends BaseEvent {

    /** CT完成实际分钟数（从到院算起） */
    private long ctMinutes;

    /** 阈值 */
    private long threshold;

    public CtTimeoutWarningEvent() {}

    public CtTimeoutWarningEvent(String eventId, Long encounterId, Long patientId,
                                  long ctMinutes, long threshold) {
        super(eventId, "CT_TIMEOUT_WARNING", encounterId, patientId);
        this.ctMinutes = ctMinutes;
        this.threshold = threshold;
    }

    // ====== Getters & Setters ======

    public long getCtMinutes() { return ctMinutes; }
    public void setCtMinutes(long ctMinutes) { this.ctMinutes = ctMinutes; }

    public long getThreshold() { return threshold; }
    public void setThreshold(long threshold) { this.threshold = threshold; }
}
