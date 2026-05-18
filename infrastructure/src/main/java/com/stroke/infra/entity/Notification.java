package com.stroke.infra.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stroke.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * 通知记录（站内信）— 对应设计文档 3.2.3 节通知模块
 * <p>
 * 用于：超时告警、溶栓准备通知、质控预警等。
 */
@TableName("notification")
public class Notification extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 通知类型: DNT_TIMEOUT / CT_TIMEOUT / THROMBOLYSIS_READY / GENERAL */
    private String notificationType;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 收件人ID */
    private String recipientId;

    /** 收件人姓名 */
    private String recipientName;

    /** 关联就诊ID */
    private Long encounterId;

    /** 关联患者ID */
    private Long patientId;

    /** 紧急程度: LOW / MEDIUM / HIGH / URGENT */
    private String priority;

    /** 发送渠道: STATION_LETTER / PAD_PUSH / SMS */
    private String channel;

    /** 已读标志 */
    private Boolean read;

    /** 已读时间 */
    private LocalDateTime readTime;

    /** 发送状态: PENDING / SENT / FAILED */
    private String sendStatus;

    // ====== Getters & Setters ======

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public Long getEncounterId() { return encounterId; }
    public void setEncounterId(Long encounterId) { this.encounterId = encounterId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }

    public LocalDateTime getReadTime() { return readTime; }
    public void setReadTime(LocalDateTime readTime) { this.readTime = readTime; }

    public String getSendStatus() { return sendStatus; }
    public void setSendStatus(String sendStatus) { this.sendStatus = sendStatus; }
}
