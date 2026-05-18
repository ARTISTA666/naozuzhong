package com.stroke.infra.service;

import com.stroke.domain.event.CtTimeoutWarningEvent;
import com.stroke.domain.event.DntTimeoutWarningEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 告警通知服务 — 对应设计文档 3.2.3 节
 * <p>
 * 处理超时事件，发送站内信/PAD推送/短信。
 * 当前为骨架实现，后续接入真实推送通道。
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    /**
     * 处理 DNT 超时红灯告警
     * <p>
     * DNT > 45分钟 → 通知：主任、质控员
     * 渠道：站内信 + PAD推送
     */
    public void handleDntTimeout(DntTimeoutWarningEvent event) {
        String message = String.format(
                "【DNT超时红灯】就诊ID=%d，患者ID=%d，DNT=%d分钟（阈值%d分钟），当前状态=%s",
                event.getEncounterId(), event.getPatientId(),
                event.getDntMinutes(), event.getThreshold(),
                event.getCurrentStatus());

        log.warn("🚨 发送DNT超时告警: {}", message);

        // TODO: 接入真实通知通道
        // 1. 查询科室主任/质控员列表
        // 2. 创建站内信通知记录
        // 3. 推送PAD通知
        // 4. 可选：短信网关
        sendStationLetter("DNT_TIMEOUT", "DNT超时告警", message, "URGENT");
        sendPadPush(event.getEncounterId(), message);
    }

    /**
     * 处理 CT 超时黄灯告警
     * <p>
     * Door-to-CT > 25分钟 → 工作站弹窗提醒
     */
    public void handleCtTimeout(CtTimeoutWarningEvent event) {
        String message = String.format(
                "【CT超时黄灯】就诊ID=%d，患者ID=%d，Door-to-CT=%d分钟（阈值%d分钟）",
                event.getEncounterId(), event.getPatientId(),
                event.getCtMinutes(), event.getThreshold());

        log.warn("💛 发送CT超时提醒: {}", message);

        // TODO: 接入真实通知通道
        // 1. 查询当前值班医生
        // 2. 工作站弹窗提醒
        sendStationLetter("CT_TIMEOUT", "CT超时提醒", message, "HIGH");
    }

    // ==================== 内部方法 ====================

    /**
     * 发送站内信
     */
    private void sendStationLetter(String type, String title, String content, String priority) {
        log.info("📧 站内信: type={}, title={}, priority={}", type, title, priority);
        // TODO: 创建 Notification 记录并持久化
    }

    /**
     * 发送 PAD 推送
     */
    private void sendPadPush(Long encounterId, String message) {
        log.info("📱 PAD推送: encounterId={}, message={}", encounterId, message);
        // TODO: 接入 WebSocket / 第三方推送
    }
}
