package com.stroke.integration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * 患者主索引匹配服务 — 对应设计文档 3.1 节 & 8.1 节
 * <p>
 * 匹配规则（按优先级）：
 * 1. 身份证号完全匹配
 * 2. 姓名 + 出生日期匹配
 * 3. 区域健康卡号匹配
 * <p>
 * 匹配规则可配置，冲突需人工确认。
 */
@Service
public class MpiMatchService {

    private static final Logger log = LoggerFactory.getLogger(MpiMatchService.class);

    /** 匹配结果置信度阈值 */
    private static final double HIGH_CONFIDENCE = 0.95;
    private static final double MEDIUM_CONFIDENCE = 0.80;

    // 模拟患者库（接入真实数据库后替换）
    private final Map<String, PatientRecord> mockPatientDb = new HashMap<>();

    public MpiMatchService() {
        // 模拟数据
        mockPatientDb.put("110101199001011234",
                new PatientRecord(1L, "张三", LocalDate.of(1990, 1, 1), "110101199001011234", "M"));
    }

    /**
     * 执行患者匹配
     *
     * @param idCard    身份证号（可能为空）
     * @param name      姓名
     * @param birthDate 出生日期
     * @param gender    性别
     * @return 匹配结果
     */
    public MatchResult matchPatient(String idCard, String name, LocalDate birthDate, String gender) {
        // 规则1: 身份证号完全匹配（最高优先级）
        if (idCard != null && !idCard.isBlank()) {
            PatientRecord record = mockPatientDb.get(idCard);
            if (record != null) {
                log.info("MPI匹配成功: 身份证号匹配, patientId={}, confidence=1.0", record.id);
                return new MatchResult(record.id, "ID_CARD", 1.0, "身份证号完全匹配");
            }
        }

        // 规则2: 姓名 + 出生日期匹配
        if (name != null && !name.isBlank() && birthDate != null) {
            List<MatchCandidate> candidates = new ArrayList<>();
            for (PatientRecord record : mockPatientDb.values()) {
                if (record.name.equals(name) && record.birthDate.equals(birthDate)) {
                    double confidence = 0.95;
                    // 性别一致则提高置信度
                    if (gender != null && gender.equals(record.gender)) {
                        confidence = 0.98;
                    }
                    candidates.add(new MatchCandidate(record.id, "NAME_DOB", confidence,
                            String.format("姓名+出生日期匹配: %s, %s", name, birthDate)));
                }
            }

            if (!candidates.isEmpty()) {
                candidates.sort((a, b) -> Double.compare(b.confidence, a.confidence));
                MatchCandidate best = candidates.get(0);
                log.info("MPI匹配: 姓名+出生日期, patientId={}, confidence={}", best.patientId, best.confidence);

                if (best.confidence >= HIGH_CONFIDENCE) {
                    return new MatchResult(best.patientId, best.matchRule, best.confidence, best.detail);
                }
                // 中等置信度需要人工确认
                return new MatchResult(null, best.matchRule, best.confidence,
                        "匹配置信度不足，需人工确认: " + best.detail);
            }
        }

        // 无匹配
        log.info("MPI无匹配: name={}, birthDate={}", name, birthDate);
        return new MatchResult(null, null, 0.0, "未找到匹配患者，将创建新档案");
    }

    /**
     * 注册新患者（MPI无匹配时创建）
     */
    public Long registerNewPatient(String name, LocalDate birthDate, String gender, String idCard) {
        // TODO: 调用 core-clinical API 创建患者
        long newId = (long) (Math.random() * 100000);
        log.info("MPI创建新患者: id={}, name={}, idCard={}", newId, name, idCard);
        return newId;
    }

    // ==================== 内部类型 ====================

    /** 患者记录（模拟） */
    private static class PatientRecord {
        final Long id;
        final String name;
        final LocalDate birthDate;
        final String idCard;
        final String gender;

        PatientRecord(Long id, String name, LocalDate birthDate, String idCard, String gender) {
            this.id = id;
            this.name = name;
            this.birthDate = birthDate;
            this.idCard = idCard;
            this.gender = gender;
        }
    }

    /** 匹配候选项 */
    private static class MatchCandidate {
        final Long patientId;
        final String matchRule;
        final double confidence;
        final String detail;

        MatchCandidate(Long patientId, String matchRule, double confidence, String detail) {
            this.patientId = patientId;
            this.matchRule = matchRule;
            this.confidence = confidence;
            this.detail = detail;
        }
    }

    /** 匹配结果 */
    public static class MatchResult {
        private final Long patientId;
        private final String matchRule;
        private final double confidence;
        private final String detail;

        public MatchResult(Long patientId, String matchRule, double confidence, String detail) {
            this.patientId = patientId;
            this.matchRule = matchRule;
            this.confidence = confidence;
            this.detail = detail;
        }

        /** 是否匹配成功 */
        public boolean isMatched() { return patientId != null; }

        /** 是否需要人工确认 */
        public boolean needsManualReview() { return confidence > 0 && confidence < HIGH_CONFIDENCE; }

        public Long getPatientId() { return patientId; }
        public String getMatchRule() { return matchRule; }
        public double getConfidence() { return confidence; }
        public String getDetail() { return detail; }
    }
}
