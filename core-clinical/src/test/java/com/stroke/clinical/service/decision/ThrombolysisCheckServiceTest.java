package com.stroke.clinical.service.decision;

import com.stroke.clinical.dto.decision.ThrombolysisCheckRequest;
import com.stroke.clinical.dto.decision.ThrombolysisCheckResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 溶栓禁忌检查服务单元测试
 */
@DisplayName("ThrombolysisCheckService — 溶栓禁忌检查")
class ThrombolysisCheckServiceTest {

    private ThrombolysisCheckService service;

    @BeforeEach
    void setUp() {
        service = new ThrombolysisCheckService();
    }

    private ThrombolysisCheckRequest createDefaultRequest() {
        ThrombolysisCheckRequest req = new ThrombolysisCheckRequest();
        req.setOnsetTime(LocalDateTime.now().minusHours(2));  // 2小时前发病
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

    @Test
    @DisplayName("无禁忌 — 结论为ELIGIBLE")
    void noContraindications() {
        ThrombolysisCheckResult result = service.check(createDefaultRequest());

        assertEquals("ELIGIBLE", result.getConclusion());
        assertTrue(result.isSuggested());
        assertEquals(0, result.getAbsoluteContraindications());
        assertEquals(0, result.getRelativeContraindications());
        assertFalse(result.getItems().stream().anyMatch(i -> i.isTriggered()));
    }

    @Test
    @DisplayName("INR > 1.7 — 绝对禁忌 CONTRAINDICATED")
    void inrHigh() {
        ThrombolysisCheckRequest req = createDefaultRequest();
        req.setInr(2.1);

        ThrombolysisCheckResult result = service.check(req);

        assertEquals("CONTRAINDICATED", result.getConclusion());
        assertFalse(result.isSuggested());
        assertTrue(result.getAbsoluteContraindications() >= 1);
        assertTrue(result.getItems().stream()
                .anyMatch(i -> "INR_HIGH".equals(i.getRuleCode()) && i.isTriggered()));
    }

    @Test
    @DisplayName("血小板 < 100 — 绝对禁忌 CONTRAINDICATED")
    void plateletLow() {
        ThrombolysisCheckRequest req = createDefaultRequest();
        req.setPlateletCount(65);

        ThrombolysisCheckResult result = service.check(req);

        assertEquals("CONTRAINDICATED", result.getConclusion());
        assertTrue(result.getItems().stream()
                .anyMatch(i -> "PLT_LOW".equals(i.getRuleCode()) && i.isTriggered()));
    }

    @Test
    @DisplayName("近期大手术 — 绝对禁忌")
    void recentSurgery() {
        ThrombolysisCheckRequest req = createDefaultRequest();
        req.setRecentMajorSurgery(true);

        ThrombolysisCheckResult result = service.check(req);

        assertEquals("CONTRAINDICATED", result.getConclusion());
        assertTrue(result.getItems().stream()
                .anyMatch(i -> "SURGERY_RECENT".equals(i.getRuleCode()) && i.isTriggered()));
    }

    @Test
    @DisplayName("颅内出血史 — 绝对禁忌")
    void ichHistory() {
        ThrombolysisCheckRequest req = createDefaultRequest();
        req.setIntracranialHemorrhageHistory(true);

        ThrombolysisCheckResult result = service.check(req);

        assertEquals("CONTRAINDICATED", result.getConclusion());
        assertTrue(result.getItems().stream()
                .anyMatch(i -> "ICH_HISTORY".equals(i.getRuleCode()) && i.isTriggered()));
    }

    @Test
    @DisplayName("超时间窗 > 4.5h — 相对禁忌 CAUTION")
    void timeWindowExceeded() {
        ThrombolysisCheckRequest req = createDefaultRequest();
        req.setOnsetTime(LocalDateTime.now().minusHours(6));  // 6小时前

        ThrombolysisCheckResult result = service.check(req);

        assertEquals("CAUTION", result.getConclusion());
        assertTrue(result.getRelativeContraindications() >= 1);
        assertTrue(result.getItems().stream()
                .anyMatch(i -> "TIME_EXCEEDED".equals(i.getRuleCode()) && i.isTriggered()));
    }

    @Test
    @DisplayName("血压 > 185/110 — 相对禁忌 CAUTION")
    void highBloodPressure() {
        ThrombolysisCheckRequest req = createDefaultRequest();
        req.setSystolicBp(195);
        req.setDiastolicBp(115);

        ThrombolysisCheckResult result = service.check(req);

        assertEquals("CAUTION", result.getConclusion());
        assertTrue(result.getItems().stream()
                .anyMatch(i -> "BP_HIGH".equals(i.getRuleCode()) && i.isTriggered()));
    }

    @Test
    @DisplayName("低血糖 < 2.7 — 相对禁忌 CAUTION")
    void lowBloodGlucose() {
        ThrombolysisCheckRequest req = createDefaultRequest();
        req.setBloodGlucose(2.1);

        ThrombolysisCheckResult result = service.check(req);

        assertEquals("CAUTION", result.getConclusion());
        assertTrue(result.getItems().stream()
                .anyMatch(i -> "GLU_LOW".equals(i.getRuleCode()) && i.isTriggered()));
    }

    @Test
    @DisplayName("高血糖 > 22.2 — 相对禁忌 CAUTION")
    void highBloodGlucose() {
        ThrombolysisCheckRequest req = createDefaultRequest();
        req.setBloodGlucose(25.0);

        ThrombolysisCheckResult result = service.check(req);

        assertEquals("CAUTION", result.getConclusion());
        assertTrue(result.getItems().stream()
                .anyMatch(i -> "GLU_HIGH".equals(i.getRuleCode()) && i.isTriggered()));
    }

    @Test
    @DisplayName("多项绝对禁忌同时触发")
    void multipleAbsolute() {
        ThrombolysisCheckRequest req = createDefaultRequest();
        req.setInr(2.5);
        req.setPlateletCount(50);
        req.setIntracranialHemorrhageHistory(true);

        ThrombolysisCheckResult result = service.check(req);

        assertEquals("CONTRAINDICATED", result.getConclusion());
        assertTrue(result.getAbsoluteContraindications() >= 3);
    }

    @Test
    @DisplayName("多项相对禁忌同时触发 → CAUTION")
    void multipleRelative() {
        ThrombolysisCheckRequest req = createDefaultRequest();
        req.setOnsetTime(LocalDateTime.now().minusHours(5));  // 超窗
        req.setSystolicBp(190);    // 高血压
        req.setBloodGlucose(23.0); // 高血糖

        ThrombolysisCheckResult result = service.check(req);

        assertEquals("CAUTION", result.getConclusion());
        assertTrue(result.getRelativeContraindications() >= 3);
    }

    @Test
    @DisplayName("INR未检测 — RELATIVE触发（标记为未知）")
    void inrUnknown() {
        ThrombolysisCheckRequest req = createDefaultRequest();
        req.setInr(null);

        ThrombolysisCheckResult result = service.check(req);

        assertTrue(result.getItems().stream()
                .anyMatch(i -> "INR_UNKNOWN".equals(i.getRuleCode()) && i.isTriggered()));
        assertEquals("CAUTION", result.getConclusion());
    }

    @Test
    @DisplayName("发病时间后于当前时间 — 异常标记")
    void onsetTimeInFuture() {
        ThrombolysisCheckRequest req = createDefaultRequest();
        req.setOnsetTime(LocalDateTime.now().plusHours(1));

        ThrombolysisCheckResult result = service.check(req);

        assertTrue(result.getItems().stream()
                .anyMatch(i -> "TIME_INVALID".equals(i.getRuleCode()) && i.isTriggered()));
        assertEquals("CONTRAINDICATED", result.getConclusion());
    }

    @Test
    @DisplayName("检查结果明细数量正确")
    void itemCount() {
        ThrombolysisCheckResult result = service.check(createDefaultRequest());

        assertEquals(7, result.getItems().size());  // 7条规则
    }
}
