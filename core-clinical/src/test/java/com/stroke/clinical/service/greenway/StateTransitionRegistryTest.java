package com.stroke.clinical.service.greenway;

import com.stroke.domain.enums.GreenwayStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 状态转换注册表单元测试
 */
@DisplayName("StateTransitionRegistry — 状态转换注册表")
class StateTransitionRegistryTest {

    private StateTransitionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new StateTransitionRegistry();
        registry.init();
    }

    // ==================== 正常路径 ====================

    @Test
    @DisplayName("WAITING_TRIAGE → TRIAGING 合法")
    void waitingTriageToTriaging() {
        assertTrue(registry.isValidTransition(GreenwayStatus.WAITING_TRIAGE, GreenwayStatus.TRIAGING));
    }

    @Test
    @DisplayName("TRIAGING → CT_ORDERED 合法")
    void triagingToCtOrdered() {
        assertTrue(registry.isValidTransition(GreenwayStatus.TRIAGING, GreenwayStatus.CT_ORDERED));
    }

    @Test
    @DisplayName("CT_ORDERED → CT_IN_PROGRESS 合法")
    void ctOrderedToCtInProgress() {
        assertTrue(registry.isValidTransition(GreenwayStatus.CT_ORDERED, GreenwayStatus.CT_IN_PROGRESS));
    }

    @Test
    @DisplayName("CT_IN_PROGRESS → CT_COMPLETED 合法")
    void ctInProgressToCtCompleted() {
        assertTrue(registry.isValidTransition(GreenwayStatus.CT_IN_PROGRESS, GreenwayStatus.CT_COMPLETED));
    }

    @Test
    @DisplayName("CT_COMPLETED → AWAITING_DECISION 合法")
    void ctCompletedToAwaitingDecision() {
        assertTrue(registry.isValidTransition(GreenwayStatus.CT_COMPLETED, GreenwayStatus.AWAITING_DECISION));
    }

    @Test
    @DisplayName("AWAITING_DECISION → THROMBOLYSIS_READY 合法")
    void awaitingDecisionToThrombolysisReady() {
        assertTrue(registry.isValidTransition(GreenwayStatus.AWAITING_DECISION, GreenwayStatus.THROMBOLYSIS_READY));
    }

    @Test
    @DisplayName("THROMBOLYSIS_READY → THROMBOLYSIS_IN_PROGRESS 合法")
    void readyToInProgress() {
        assertTrue(registry.isValidTransition(GreenwayStatus.THROMBOLYSIS_READY, GreenwayStatus.THROMBOLYSIS_IN_PROGRESS));
    }

    @Test
    @DisplayName("THROMBOLYSIS_IN_PROGRESS → THROMBOLYSIS_COMPLETED 合法")
    void inProgressToCompleted() {
        assertTrue(registry.isValidTransition(GreenwayStatus.THROMBOLYSIS_IN_PROGRESS, GreenwayStatus.THROMBOLYSIS_COMPLETED));
    }

    @Test
    @DisplayName("THROMBOLYSIS_COMPLETED → COMPLETED 合法")
    void thrombolysisCompletedToCompleted() {
        assertTrue(registry.isValidTransition(GreenwayStatus.THROMBOLYSIS_COMPLETED, GreenwayStatus.COMPLETED));
    }

    @Test
    @DisplayName("完整正常路径 — 全链路转换")
    void fullNormalPath() {
        GreenwayStatus[] path = {
                GreenwayStatus.WAITING_TRIAGE,
                GreenwayStatus.TRIAGING,
                GreenwayStatus.CT_ORDERED,
                GreenwayStatus.CT_IN_PROGRESS,
                GreenwayStatus.CT_COMPLETED,
                GreenwayStatus.AWAITING_DECISION,
                GreenwayStatus.THROMBOLYSIS_READY,
                GreenwayStatus.THROMBOLYSIS_IN_PROGRESS,
                GreenwayStatus.THROMBOLYSIS_COMPLETED,
                GreenwayStatus.COMPLETED
        };
        for (int i = 0; i < path.length - 1; i++) {
            GreenwayStatus from = path[i];
            GreenwayStatus to = path[i + 1];
            assertTrue(registry.isValidTransition(from, to),
                    () -> "路径不通: " + from + " → " + to);
        }
    }

    // ==================== 异常路径 ====================

    @Test
    @DisplayName("CT_COMPLETED → CT_REPEAT_REQUIRED 合法（重扫）")
    void ctCompletedToRepeatRequired() {
        assertTrue(registry.isValidTransition(GreenwayStatus.CT_COMPLETED, GreenwayStatus.CT_REPEAT_REQUIRED));
    }

    @Test
    @DisplayName("CT_REPEAT_REQUIRED → CT_ORDERED 合法（重新开单）")
    void repeatRequiredToCtOrdered() {
        assertTrue(registry.isValidTransition(GreenwayStatus.CT_REPEAT_REQUIRED, GreenwayStatus.CT_ORDERED));
    }

    @Test
    @DisplayName("从任意中间状态 → TREATMENT_ABORTED 合法")
    void anyStateToAborted() {
        GreenwayStatus[] nonTerminalStates = {
                GreenwayStatus.WAITING_TRIAGE,
                GreenwayStatus.TRIAGING,
                GreenwayStatus.CT_ORDERED,
                GreenwayStatus.CT_IN_PROGRESS,
                GreenwayStatus.CT_COMPLETED,
                GreenwayStatus.AWAITING_DECISION,
                GreenwayStatus.THROMBOLYSIS_READY,
                GreenwayStatus.THROMBOLYSIS_IN_PROGRESS
        };
        for (GreenwayStatus state : nonTerminalStates) {
            assertTrue(registry.isValidTransition(state, GreenwayStatus.TREATMENT_ABORTED),
                    () -> state + " → TREATMENT_ABORTED 应合法");
        }
    }

    @Test
    @DisplayName("从任意中间状态 → TRANSFERRED 合法")
    void anyStateToTransferred() {
        GreenwayStatus[] nonTerminalStates = {
                GreenwayStatus.WAITING_TRIAGE,
                GreenwayStatus.TRIAGING,
                GreenwayStatus.CT_ORDERED,
                GreenwayStatus.CT_IN_PROGRESS,
                GreenwayStatus.CT_COMPLETED,
                GreenwayStatus.AWAITING_DECISION,
                GreenwayStatus.THROMBOLYSIS_READY,
                GreenwayStatus.THROMBOLYSIS_IN_PROGRESS
        };
        for (GreenwayStatus state : nonTerminalStates) {
            assertTrue(registry.isValidTransition(state, GreenwayStatus.TRANSFERRED),
                    () -> state + " → TRANSFERRED 应合法");
        }
    }

    // ==================== 非法转换 ====================

    @Test
    @DisplayName("WAITING_TRIAGE → THROMBOLYSIS_READY 非法（跳步）")
    void skipStepIllegal() {
        assertFalse(registry.isValidTransition(GreenwayStatus.WAITING_TRIAGE, GreenwayStatus.THROMBOLYSIS_READY));
    }

    @Test
    @DisplayName("COMPLETED → 其他状态 非法（终止态不可逆）")
    void completedCannotGoBack() {
        assertFalse(registry.isValidTransition(GreenwayStatus.COMPLETED, GreenwayStatus.AWAITING_DECISION));
        assertFalse(registry.isValidTransition(GreenwayStatus.COMPLETED, GreenwayStatus.TRIAGING));
    }

    @Test
    @DisplayName("TREATMENT_ABORTED → 其他状态 非法（终止态不可出）")
    void abortedCannotTransition() {
        assertFalse(registry.isValidTransition(GreenwayStatus.TREATMENT_ABORTED, GreenwayStatus.CT_ORDERED));
    }

    @Test
    @DisplayName("CT_FAILED → CT_COMPLETED 非法（设备故障必须重扫）")
    void ctFailedToCompletedIllegal() {
        assertFalse(registry.isValidTransition(GreenwayStatus.CT_FAILED, GreenwayStatus.CT_COMPLETED));
    }

    // ==================== 可用操作查询 ====================

    @Test
    @DisplayName("TRIAGING 状态可用操作应包含 CT_ORDERED 和异常终止")
    void triagingAvailableTargets() {
        Set<GreenwayStatus> targets = registry.getAvailableTargets(GreenwayStatus.TRIAGING);
        assertAll(
                () -> assertTrue(targets.contains(GreenwayStatus.CT_ORDERED)),
                () -> assertTrue(targets.contains(GreenwayStatus.TREATMENT_ABORTED)),
                () -> assertTrue(targets.contains(GreenwayStatus.TRANSFERRED))
        );
    }

    @Test
    @DisplayName("终止态可用操作为空")
    void terminalStateNoTargets() {
        assertTrue(registry.getAvailableTargets(GreenwayStatus.COMPLETED).isEmpty());
        assertTrue(registry.getAvailableTargets(GreenwayStatus.TREATMENT_ABORTED).isEmpty());
    }

    // ==================== 重扫回退路径 ====================

    @Test
    @DisplayName("CT_COMPLETED → CT_REPEAT_REQUIRED → CT_ORDERED 重扫全链路")
    void rescanPath() {
        assertTrue(registry.isValidTransition(GreenwayStatus.CT_COMPLETED, GreenwayStatus.CT_REPEAT_REQUIRED));
        assertTrue(registry.isValidTransition(GreenwayStatus.CT_REPEAT_REQUIRED, GreenwayStatus.CT_ORDERED));
    }

    // ==================== 备注规则检查 ====================

    @Test
    @DisplayName("TREATMENT_ABORTED 转换规则要求备注")
    void abortRequiresRemark() {
        StateTransitionRegistry.TransitionRule rule =
                registry.getRule(GreenwayStatus.WAITING_TRIAGE, GreenwayStatus.TREATMENT_ABORTED);
        assertNotNull(rule);
        assertTrue(rule.isRequireRemark());
    }

    @Test
    @DisplayName("CT_ORDERED 转换规则不要求备注")
    void ctOrderedNoRemark() {
        StateTransitionRegistry.TransitionRule rule =
                registry.getRule(GreenwayStatus.TRIAGING, GreenwayStatus.CT_ORDERED);
        assertNotNull(rule);
        assertFalse(rule.isRequireRemark());
    }

    @Test
    @DisplayName("THROMBOLYSIS_READY → IN_PROGRESS 需要神内主治角色")
    void thrombolysisRequiresRole() {
        StateTransitionRegistry.TransitionRule rule =
                registry.getRule(GreenwayStatus.THROMBOLYSIS_READY, GreenwayStatus.THROMBOLYSIS_IN_PROGRESS);
        assertNotNull(rule);
        assertEquals("神经内科主治医师", rule.getRequiredRole());
    }
}
