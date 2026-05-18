package com.stroke.clinical.service.greenway;

import com.stroke.clinical.dto.StateChangeRequest;
import com.stroke.common.BusinessException;
import com.stroke.domain.entity.StrokeGreenway;
import com.stroke.domain.enums.GreenwayStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 绿道状态机引擎单元测试
 */
@DisplayName("GreenwayStateMachine — 状态机引擎")
class GreenwayStateMachineTest {

    private StateTransitionRegistry registry;
    private GreenwayStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        registry = new StateTransitionRegistry();
        registry.init();
        stateMachine = new GreenwayStateMachine(registry);
    }

    private StrokeGreenway createGreenwayWithStatus(GreenwayStatus status) {
        StrokeGreenway g = new StrokeGreenway();
        g.setId(1L);
        g.setEncounterId(100L);
        g.setPatientId(1000L);
        g.setStatus(status.name());
        g.setDoorTime(LocalDateTime.now());
        g.setVersion(0);
        return g;
    }

    private StateChangeRequest request(String targetStatus) {
        StateChangeRequest r = new StateChangeRequest();
        r.setTargetStatus(targetStatus);
        r.setOperatorId("test-doctor");
        r.setOperatorName("测试医生");
        return r;
    }

    @Test
    @DisplayName("正常路径: WAITING_TRIAGE → TRIAGING")
    void normalTransition() {
        StrokeGreenway g = createGreenwayWithStatus(GreenwayStatus.WAITING_TRIAGE);
        StateChangeRequest req = request("TRIAGING");

        GreenwayStateMachine.TransitionResult result = stateMachine.transition(g, req);

        assertEquals(GreenwayStatus.WAITING_TRIAGE, result.getFromStatus());
        assertEquals(GreenwayStatus.TRIAGING, result.getToStatus());
        assertEquals(GreenwayStatus.TRIAGING.name(), g.getStatus());
    }

    @Test
    @DisplayName("CT_ORDERED 自动记录 ctOrderTime")
    void autoTimestampCtOrdered() {
        StrokeGreenway g = createGreenwayWithStatus(GreenwayStatus.TRIAGING);
        assertNull(g.getCtOrderTime());

        stateMachine.transition(g, request("CT_ORDERED"));

        assertNotNull(g.getCtOrderTime());
        assertEquals(GreenwayStatus.CT_ORDERED.name(), g.getStatus());
    }

    @Test
    @DisplayName("THROMBOLYSIS_READY → IN_PROGRESS 自动记录 needleTime")
    void autoTimestampNeedleTime() {
        StrokeGreenway g = createGreenwayWithStatus(GreenwayStatus.THROMBOLYSIS_READY);
        assertNull(g.getNeedleTime());

        stateMachine.transition(g, request("THROMBOLYSIS_IN_PROGRESS"));

        assertNotNull(g.getNeedleTime());
    }

    @Test
    @DisplayName("非法转换抛出 BusinessException")
    void illegalTransitionThrows() {
        StrokeGreenway g = createGreenwayWithStatus(GreenwayStatus.WAITING_TRIAGE);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> stateMachine.transition(g, request("THROMBOLYSIS_READY")));
        assertTrue(ex.getMessage().contains("非法状态转换"));
    }

    @Test
    @DisplayName("中止转换要求备注，无备注则抛异常")
    void abortRequiresRemark() {
        StrokeGreenway g = createGreenwayWithStatus(GreenwayStatus.CT_COMPLETED);
        StateChangeRequest req = request("TREATMENT_ABORTED");
        req.setRemark(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> stateMachine.transition(g, req));
        assertTrue(ex.getMessage().contains("必须填写操作原因"));
    }

    @Test
    @DisplayName("中止转换有备注则成功")
    void abortWithRemarkSucceeds() {
        StrokeGreenway g = createGreenwayWithStatus(GreenwayStatus.CT_COMPLETED);
        StateChangeRequest req = request("TREATMENT_ABORTED");
        req.setRemark("家属拒绝继续治疗");

        GreenwayStateMachine.TransitionResult result = stateMachine.transition(g, req);

        assertEquals(GreenwayStatus.TREATMENT_ABORTED, result.getToStatus());
        assertEquals("家属拒绝继续治疗", g.getAbortReason());
    }

    @Test
    @DisplayName("转院转换要求备注")
    void transferRequiresRemark() {
        StrokeGreenway g = createGreenwayWithStatus(GreenwayStatus.AWAITING_DECISION);
        StateChangeRequest req = request("TRANSFERRED");
        req.setRemark(null);

        assertThrows(BusinessException.class, () -> stateMachine.transition(g, req));
    }

    @Test
    @DisplayName("重扫路径: CT_COMPLETED → CT_REPEAT_REQUIRED → CT_ORDERED")
    void rescanPath() {
        // Step 1: CT_COMPLETED → CT_REPEAT_REQUIRED
        StrokeGreenway g1 = createGreenwayWithStatus(GreenwayStatus.CT_COMPLETED);
        StateChangeRequest req1 = request("CT_REPEAT_REQUIRED");
        req1.setRemark("影像质量差，有运动伪影");
        stateMachine.transition(g1, req1);
        assertEquals(GreenwayStatus.CT_REPEAT_REQUIRED.name(), g1.getStatus());

        // Step 2: CT_REPEAT_REQUIRED → CT_ORDERED
        StateChangeRequest req2 = request("CT_ORDERED");
        stateMachine.transition(g1, req2);
        assertEquals(GreenwayStatus.CT_ORDERED.name(), g1.getStatus());
    }

    @Test
    @DisplayName("终止态校验")
    void isTerminal() {
        assertTrue(stateMachine.isTerminal(GreenwayStatus.COMPLETED));
        assertTrue(stateMachine.isTerminal(GreenwayStatus.TREATMENT_ABORTED));
        assertTrue(stateMachine.isTerminal(GreenwayStatus.TRANSFERRED));
        assertTrue(stateMachine.isTerminal(GreenwayStatus.THROMBOLYSIS_COMPLETED));
        assertFalse(stateMachine.isTerminal(GreenwayStatus.CT_ORDERED));
        assertFalse(stateMachine.isTerminal(GreenwayStatus.AWAITING_DECISION));
    }

    @Test
    @DisplayName("状态解析 — 大小写不敏感")
    void parseStatusCaseInsensitive() {
        assertEquals(GreenwayStatus.CT_ORDERED, stateMachine.parseStatus("ct_ordered"));
        assertEquals(GreenwayStatus.CT_ORDERED, stateMachine.parseStatus("CT_ORDERED"));
        assertEquals(GreenwayStatus.THROMBOLYSIS_READY, stateMachine.parseStatus("thrombolysis_ready"));
    }

    @Test
    @DisplayName("状态解析 — 非法值抛出异常")
    void parseStatusInvalid() {
        assertThrows(BusinessException.class, () -> stateMachine.parseStatus("INVALID_STATUS"));
        assertThrows(BusinessException.class, () -> stateMachine.parseStatus(""));
    }

    @Test
    @DisplayName("历史快照生成")
    void createHistory() {
        StrokeGreenway g = createGreenwayWithStatus(GreenwayStatus.CT_ORDERED);
        StrokeGreenwayHistory history = stateMachine.createHistory(
                g, GreenwayStatus.TRIAGING, GreenwayStatus.CT_ORDERED, "test-user", "开CT申请");

        assertNotNull(history);
        assertEquals(g.getId(), history.getGreenwayId());
        assertEquals("STATE_CHANGE", history.getActionType());
        assertEquals("TRIAGING", history.getFromStatus());
        assertEquals("CT_ORDERED", history.getToStatus());
        assertEquals("test-user", history.getOperatorId());
        assertEquals("开CT申请", history.getRemark());
        assertNotNull(history.getActionTime());
        assertNotNull(history.getSnapshotJson());
    }

    @Test
    @DisplayName("决策时间仅在进入 AWAITING_DECISION 时自动记录")
    void decisionTimeRecordedOnAwaitingDecision() {
        StrokeGreenway g = createGreenwayWithStatus(GreenwayStatus.CT_COMPLETED);
        assertNull(g.getDecisionTime());

        stateMachine.transition(g, request("AWAITING_DECISION"));
        assertNotNull(g.getDecisionTime());
    }

    @Test
    @DisplayName("获取可用目标状态")
    void getAvailableTargets() {
        StrokeGreenway g = createGreenwayWithStatus(GreenwayStatus.TRIAGING);
        var targets = stateMachine.getAvailableTargetNames(GreenwayStatus.TRIAGING);

        assertTrue(targets.contains("CT_ORDERED"));
        assertTrue(targets.contains("TREATMENT_ABORTED"));
        assertTrue(targets.contains("TRANSFERRED"));
    }
}
