package com.stroke.clinical.service.greenway;

import com.stroke.domain.enums.GreenwayStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 状态转换注册表 — 定义绿道所有合法的状态转换规则
 * <p>
 * 对应设计文档 3.2.1 节状态机定义。
 * 包含正常路径和异常路径，支持从任意状态到异常终止态的转换。
 */
@Component
public class StateTransitionRegistry {

    /** 转换规则映射: fromStatus -> {toStatus -> transitionRule} */
    private final Map<GreenwayStatus, Map<GreenwayStatus, TransitionRule>> transitions = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        registerNormalPath();
        registerAbnormalPath();
    }

    // ==================== 正常路径 ====================

    private void registerNormalPath() {
        // WAITING_TRIAGE → TRIAGING: 分诊台完成分诊
        addTransition(GreenwayStatus.WAITING_TRIAGE, GreenwayStatus.TRIAGING,
                rule().requireRemark(false));

        // TRIAGING → CT_ORDERED: 医生一键开CT
        addTransition(GreenwayStatus.TRIAGING, GreenwayStatus.CT_ORDERED,
                rule().requireRemark(false).autoTimestamp("ctOrderTime"));

        // CT_ORDERED → CT_IN_PROGRESS: CT开始扫描
        addTransition(GreenwayStatus.CT_ORDERED, GreenwayStatus.CT_IN_PROGRESS,
                rule().requireRemark(false));

        // CT_IN_PROGRESS → CT_COMPLETED: CT完成
        addTransition(GreenwayStatus.CT_IN_PROGRESS, GreenwayStatus.CT_COMPLETED,
                rule().requireRemark(false).autoTimestamp("ctCompleteTime"));

        // CT_COMPLETED → AWAITING_DECISION: 医生查看影像，准备决策
        addTransition(GreenwayStatus.CT_COMPLETED, GreenwayStatus.AWAITING_DECISION,
                rule().requireRemark(false));

        // AWAITING_DECISION → THROMBOLYSIS_READY: CDS检查通过，准备溶栓
        addTransition(GreenwayStatus.AWAITING_DECISION, GreenwayStatus.THROMBOLYSIS_READY,
                rule().requireRemark(false));

        // THROMBOLYSIS_READY → THROMBOLYSIS_IN_PROGRESS: 溶栓开始
        addTransition(GreenwayStatus.THROMBOLYSIS_READY, GreenwayStatus.THROMBOLYSIS_IN_PROGRESS,
                rule().requireRemark(false).autoTimestamp("needleTime")
                        .requiredRole("神经内科主治医师"));

        // THROMBOLYSIS_IN_PROGRESS → THROMBOLYSIS_COMPLETED: 溶栓完成
        addTransition(GreenwayStatus.THROMBOLYSIS_IN_PROGRESS, GreenwayStatus.THROMBOLYSIS_COMPLETED,
                rule().requireRemark(false));

        // THROMBOLYSIS_COMPLETED → COMPLETED: 绿道结束
        addTransition(GreenwayStatus.THROMBOLYSIS_COMPLETED, GreenwayStatus.COMPLETED,
                rule().requireRemark(false));
    }

    // ==================== 异常路径 ====================

    private void registerAbnormalPath() {
        // CT_COMPLETED → CT_REPEAT_REQUIRED: 影像质量差需重扫
        addTransition(GreenwayStatus.CT_COMPLETED, GreenwayStatus.CT_REPEAT_REQUIRED,
                rule().requireRemark(true));

        // CT_REPEAT_REQUIRED → CT_ORDERED: 重新开CT
        addTransition(GreenwayStatus.CT_REPEAT_REQUIRED, GreenwayStatus.CT_ORDERED,
                rule().requireRemark(false));

        // CT_FAILED → CT_ORDERED: 设备故障后重开
        addTransition(GreenwayStatus.CT_FAILED, GreenwayStatus.CT_ORDERED,
                rule().requireRemark(true));

        // 从任意状态 → TREATMENT_ABORTED: 治疗中止（家属拒绝、抢救插管等）
        registerUniversalAbortTransitions();

        // 从任意状态 → TRANSFERRED: 转院
        registerUniversalTransferTransitions();
    }

    /**
     * 从所有非终止状态 → TREATMENT_ABORTED
     */
    private void registerUniversalAbortTransitions() {
        List<GreenwayStatus> abortableFrom = Arrays.asList(
                GreenwayStatus.WAITING_TRIAGE,
                GreenwayStatus.TRIAGING,
                GreenwayStatus.CT_ORDERED,
                GreenwayStatus.CT_IN_PROGRESS,
                GreenwayStatus.CT_COMPLETED,
                GreenwayStatus.CT_FAILED,
                GreenwayStatus.CT_REPEAT_REQUIRED,
                GreenwayStatus.AWAITING_DECISION,
                GreenwayStatus.THROMBOLYSIS_READY,
                GreenwayStatus.THROMBOLYSIS_IN_PROGRESS
        );
        for (GreenwayStatus from : abortableFrom) {
            addTransition(from, GreenwayStatus.TREATMENT_ABORTED,
                    rule().requireRemark(true).terminal(true));
        }
    }

    /**
     * 从所有非终止状态 → TRANSFERRED
     */
    private void registerUniversalTransferTransitions() {
        List<GreenwayStatus> transferableFrom = Arrays.asList(
                GreenwayStatus.WAITING_TRIAGE,
                GreenwayStatus.TRIAGING,
                GreenwayStatus.CT_ORDERED,
                GreenwayStatus.CT_IN_PROGRESS,
                GreenwayStatus.CT_COMPLETED,
                GreenwayStatus.CT_FAILED,
                GreenwayStatus.CT_REPEAT_REQUIRED,
                GreenwayStatus.AWAITING_DECISION,
                GreenwayStatus.THROMBOLYSIS_READY,
                GreenwayStatus.THROMBOLYSIS_IN_PROGRESS
        );
        for (GreenwayStatus from : transferableFrom) {
            addTransition(from, GreenwayStatus.TRANSFERRED,
                    rule().requireRemark(true).terminal(true));
        }
    }

    // ==================== 注册方法 ====================

    private void addTransition(GreenwayStatus from, GreenwayStatus to, TransitionRule rule) {
        transitions.computeIfAbsent(from, k -> new ConcurrentHashMap<>())
                .put(to, rule);
    }

    // ==================== 查询方法 ====================

    /**
     * 检查转换是否合法
     */
    public boolean isValidTransition(GreenwayStatus from, GreenwayStatus to) {
        Map<GreenwayStatus, TransitionRule> fromMap = transitions.get(from);
        return fromMap != null && fromMap.containsKey(to);
    }

    /**
     * 获取转换规则
     */
    public TransitionRule getRule(GreenwayStatus from, GreenwayStatus to) {
        Map<GreenwayStatus, TransitionRule> fromMap = transitions.get(from);
        if (fromMap == null) {
            return null;
        }
        return fromMap.get(to);
    }

    /**
     * 获取指定状态的所有可选目标状态
     */
    public Set<GreenwayStatus> getAvailableTargets(GreenwayStatus current) {
        Map<GreenwayStatus, TransitionRule> fromMap = transitions.get(current);
        if (fromMap == null) {
            return Collections.emptySet();
        }
        return fromMap.keySet();
    }

    /**
     * 获取指定状态的所有可选目标状态名（前端可用操作列表）
     */
    public Set<String> getAvailableTargetNames(GreenwayStatus current) {
        return getAvailableTargets(current).stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    // ==================== 转换规则定义 ====================

    public static class TransitionRule {
        /** 是否必须填写备注 */
        private boolean requireRemark;

        /** 是否自动记录某个时间戳字段 */
        private String autoTimestampField;

        /** 是否为终止态 */
        private boolean terminal;

        /** 所需角色（为后续ABAC预留） */
        private String requiredRole;

        public boolean isRequireRemark() { return requireRemark; }
        public String getAutoTimestampField() { return autoTimestampField; }
        public boolean isTerminal() { return terminal; }
        public String getRequiredRole() { return requiredRole; }
    }

    // ==================== Builder ====================

    private TransitionRule rule() {
        return new TransitionRule();
    }

    /**
     * 创建带备注要求的规则
     */
    public static TransitionRuleBuilder builder() {
        return new TransitionRuleBuilder();
    }

    public static class TransitionRuleBuilder {
        private boolean requireRemark;
        private String autoTimestampField;
        private boolean terminal;
        private String requiredRole;

        public TransitionRuleBuilder requireRemark(boolean requireRemark) {
            this.requireRemark = requireRemark;
            return this;
        }

        public TransitionRuleBuilder autoTimestamp(String field) {
            this.autoTimestampField = field;
            return this;
        }

        public TransitionRuleBuilder terminal(boolean terminal) {
            this.terminal = terminal;
            return this;
        }

        public TransitionRuleBuilder requiredRole(String role) {
            this.requiredRole = role;
            return this;
        }

        public TransitionRule build() {
            TransitionRule rule = new TransitionRule();
            rule.requireRemark = this.requireRemark;
            rule.autoTimestampField = this.autoTimestampField;
            rule.terminal = this.terminal;
            rule.requiredRole = this.requiredRole;
            return rule;
        }
    }
}
