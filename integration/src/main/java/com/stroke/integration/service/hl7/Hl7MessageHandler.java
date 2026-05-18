package com.stroke.integration.service.hl7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * HL7 v2 消息处理器 — 对应设计文档 3.5 节
 * <p>
 * 职责：接收 HIS 发送的 HL7 v2 消息，解析并转发至 core-clinical。
 * <p>
 * 支持的消息类型：
 * - ADT^A01: 患者入院登记
 * - ADT^A02: 患者转科
 * - ADT^A03: 患者出院
 * - ORM^O01: 医嘱
 * <p>
 * 异常兼容：对外部系统不可用时提供降级方案（手动录入）。
 */
@Service
public class Hl7MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(Hl7MessageHandler.class);

    /**
     * 处理 HL7 v2 消息
     *
     * @param rawMessage HL7 v2 原始消息文本
     * @return 处理结果
     */
    public Hl7Result handleMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return Hl7Result.failed("消息内容为空");
        }

        log.info("接收到HL7消息: length={}", rawMessage.length());

        try {
            // 解析消息类型
            String messageType = parseMessageType(rawMessage);
            if (messageType == null) {
                return Hl7Result.failed("无法解析消息类型");
            }

            log.debug("HL7消息类型: {}", messageType);

            // 按类型分发处理
            switch (messageType) {
                case "ADT^A01":
                    return handleAdmission(rawMessage);
                case "ADT^A02":
                    return handleTransfer(rawMessage);
                case "ADT^A03":
                    return handleDischarge(rawMessage);
                case "ORM^O01":
                    return handleOrder(rawMessage);
                default:
                    log.warn("未支持的HL7消息类型: {}", messageType);
                    return Hl7Result.failed("未支持的消息类型: " + messageType);
            }
        } catch (Exception e) {
            log.error("HL7消息处理异常: ", e);
            return Hl7Result.failed("处理异常: " + e.getMessage());
        }
    }

    /**
     * 解析 MSH-9 消息类型
     */
    private String parseMessageType(String rawMessage) {
        // 简单解析: 从 MSH 段提取 MSH-9
        // 实际应用应使用 HL7 解析库（如 HAPI）
        for (String line : rawMessage.split("\n|\r")) {
            if (line.startsWith("MSH")) {
                String[] fields = line.split("\\|");
                if (fields.length > 8) {
                    return fields[8]; // MSH-9: 消息类型
                }
            }
        }
        return null;
    }

    private Hl7Result handleAdmission(String rawMessage) {
        // TODO: 提取患者信息，调用 MpiMatchService 匹配，通知 core-clinical
        log.info("处理入院登记消息");
        return Hl7Result.success("ADT^A01", "入院登记处理完成");
    }

    private Hl7Result handleTransfer(String rawMessage) {
        // TODO: 更新 encounter 科室信息
        log.info("处理转科消息");
        return Hl7Result.success("ADT^A02", "转科处理完成");
    }

    private Hl7Result handleDischarge(String rawMessage) {
        // TODO: 更新 encounter 状态，触发 EncounterCompleted 事件
        log.info("处理出院消息");
        return Hl7Result.success("ADT^A03", "出院处理完成");
    }

    private Hl7Result handleOrder(String rawMessage) {
        // TODO: 提取医嘱信息，转给 core-clinical
        log.info("处理医嘱消息");
        return Hl7Result.success("ORM^O01", "医嘱处理完成");
    }

    // ==================== 结果类型 ====================

    public static class Hl7Result {
        private final boolean success;
        private final String messageType;
        private final String message;

        private Hl7Result(boolean success, String messageType, String message) {
            this.success = success;
            this.messageType = messageType;
            this.message = message;
        }

        public static Hl7Result success(String messageType, String message) {
            return new Hl7Result(true, messageType, message);
        }

        public static Hl7Result failed(String message) {
            return new Hl7Result(false, null, message);
        }

        public boolean isSuccess() { return success; }
        public String getMessageType() { return messageType; }
        public String getMessage() { return message; }
    }
}
