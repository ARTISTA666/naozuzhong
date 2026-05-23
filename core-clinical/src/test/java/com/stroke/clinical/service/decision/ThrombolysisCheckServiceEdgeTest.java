package com.stroke.clinical.service.decision;

import com.stroke.clinical.dto.decision.ThrombolysisCheckRequest;
import com.stroke.clinical.dto.decision.ThrombolysisCheckResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 溶栓禁忌检查 — 新增边界值 + 组合测试
 */
@DisplayName("ThrombolysisCheckService — 边界值与组合")
class ThrombolysisCheckServiceEdgeTest {

    private ThrombolysisCheckService service;

    @BeforeEach
    void setUp() {
        service = new ThrombolysisCheckService();
    }

    @Test
    @DisplayName("INR 恰好等于 1.7 — 非禁忌")
    void inrAtThreshold() {
        ThrombolysisCheckRequest req = createDefault();
        req.setInr(1.7);
        ThrombolysisCheckResult res = service.check(req);
        assertFalse(res.getItems().stream()
                .anyMatch(i -> "INR_HIGH".equals(i.getRuleCode())));
    }

    @Test
    @DisplayName("INR 恰好大于 1.7 一位 — 绝对禁忌")
    void inrJustAboveThreshold() {
        ThrombolysisCheckRequest req = createDefault();
        req.setInr(1.71);
        ThrombolysisCheckResult res = service.check(req);
        assertTrue(res.getItems().stream()
                .anyMatch(i -> "INR_HIGH".equals(i.getRuleCode()) && i.isTriggered()));
        assertEquals("CONTRAINDICATED", res.getConclusion());
    }

    @Test
    @DisplayName("PLT 恰好等于 100 — 非禁忌")
    void pltAtThreshold() {
        ThrombolysisCheckRequest req = createDefault();
        req.setPlateletCount(100);
        ThrombolysisCheckResult res = service.check(req);
        assertFalse(res.getItems().stream()
                .anyMatch(i -> "PLT_LOW".equals(i.getRuleCode())));
    }

    @Test
    @DisplayName("发病时间恰好 4.5h — 非超窗")
    void onsetExactlyAtThreshold() {
        ThrombolysisCheckRequest req = createDefault();
        req.setOnsetTime(LocalDateTime.now().minusMinutes(270)); // 4.5h
        ThrombolysisCheckResult res = service.check(req);
        assertFalse(res.getItems().stream()
                .anyMatch(i -> "TIME_EXCEEDED".equals(i.getRuleCode())));
    }

    @Test
    @DisplayName("发病时间超过 4.5h 1秒 — 超窗")
    void onsetJustOverThreshold() {
        ThrombolysisCheckRequest req = createDefault();
        req.setOnsetTime(LocalDateTime.now().minusMinutes(270).minusSeconds(1));
        ThrombolysisCheckResult res = service.check(req);
        assertTrue(res.getItems().stream()
                .anyMatch(i -> "TIME_EXCEEDED".equals(i.getRuleCode()) && i.isTriggered()));
        assertEquals("CAUTION", res.getConclusion());
    }

    @Test
    @DisplayName("BP 收缩压恰好 185 + 舒张压 110 — 非禁忌")
    void bpAtThreshold() {
        ThrombolysisCheckRequest req = createDefault();
        req.setSystolicBp(185);
        req.setDiastolicBp(110);
        ThrombolysisCheckResult res = service.check(req);
        assertFalse(res.getItems().stream()
                .anyMatch(i -> "BP_HIGH".equals(i.getRuleCode())));
    }

    @Test
    @DisplayName("BP 收缩压 186 — 相对禁忌")
    void bpJustAboveSystolic() {
        ThrombolysisCheckRequest req = createDefault();
        req.setSystolicBp(186);
        req.setDiastolicBp(110);
        ThrombolysisCheckResult res = service.check(req);
        assertTrue(res.getItems().stream()
                .anyMatch(i -> "BP_HIGH".equals(i.getRuleCode()) && i.isTriggered()));
    }

    @Test
    @DisplayName("所有参数缺失 — CAUTION + 多项未知")
    void allMissing() {
        ThrombolysisCheckRequest req = new ThrombolysisCheckRequest();
        req.setOnsetTime(LocalDateTime.now().minusHours(2));
        ThrombolysisCheckResult res = service.check(req);
        assertEquals("CAUTION", res.getConclusion());
        assertTrue(res.getRelativeContraindications() >= 3);
    }

    @Test
    @DisplayName("BP 舒张压单独 111 — 相对禁忌")
    void diastolicJustAbove() {
        ThrombolysisCheckRequest req = createDefault();
        req.setSystolicBp(180);
        req.setDiastolicBp(111);
        ThrombolysisCheckResult res = service.check(req);
        assertTrue(res.getItems().stream()
                .anyMatch(i -> "BP_HIGH".equals(i.getRuleCode()) && i.isTriggered()));
    }

    @Test
    @DisplayName("血糖 2.7 — 非低血糖禁忌")
    void glucoseLowAtThreshold() {
        ThrombolysisCheckRequest req = createDefault();
        req.setBloodGlucose(2.7);
        ThrombolysisCheckResult res = service.check(req);
        assertFalse(res.getItems().stream()
                .anyMatch(i -> "GLU_LOW".equals(i.getRuleCode())));
    }

    @Test
    @DisplayName("血糖 22.2 — 非高血糖禁忌")
    void glucoseHighAtThreshold() {
        ThrombolysisCheckRequest req = createDefault();
        req.setBloodGlucose(22.2);
        ThrombolysisCheckResult res = service.check(req);
        assertFalse(res.getItems().stream()
                .anyMatch(i -> "GLU_HIGH".equals(i.getRuleCode())));
    }

    @Test
    @DisplayName("绝对+相对同时触发 → CONTRAINDICATED")
    void absoluteAndRelative() {
        ThrombolysisCheckRequest req = createDefault();
        req.setInr(2.0);        // 绝对
        req.setSystolicBp(190); // 相对
        ThrombolysisCheckResult res = service.check(req);
        assertEquals("CONTRAINDICATED", res.getConclusion());
        assertTrue(res.getAbsoluteContraindications() >= 1);
        assertTrue(res.getRelativeContraindications() >= 1);
    }

    @Test
    @DisplayName("PLT = 0 — 绝对禁忌")
    void plateletZero() {
        ThrombolysisCheckRequest req = createDefault();
        req.setPlateletCount(0);
        ThrombolysisCheckResult res = service.check(req);
        assertTrue(res.getItems().stream()
                .anyMatch(i -> "PLT_LOW".equals(i.getRuleCode()) && i.isTriggered()));
    }

    private ThrombolysisCheckRequest createDefault() {
        ThrombolysisCheckRequest req = new ThrombolysisCheckRequest();
        req.setOnsetTime(LocalDateTime.now().minusHours(2));
        req.setInr(1.1);
        req.setPlateletCount(220);
        req.setBloodGlucose(5.6);
        req.setSystolicBp(135);
        req.setDiastolicBp(85);
        req.setRecentMajorSurgery(false);
        req.setIntracranialHemorrhageHistory(false);
        req.setOnAnticoagulant(false);
        return req;
    }
}
