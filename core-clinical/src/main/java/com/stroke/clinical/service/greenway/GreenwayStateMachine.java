package com.stroke.clinical.service.greenway;

import com.stroke.clinical.dto.StateChangeRequest;
import com.stroke.common.BusinessException;
import com.stroke.domain.entity.StrokeGreenway;
import com.stroke.domain.entity.StrokeGreenwayHistory;
import com.stroke.domain.enums.GreenwayStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * 绿道状态机引擎 — 对应设计文档 3.2.1 节
 * <p>
 * 职责：
 * 1. 校验状态转换是否合法（通过 StateTransitionRegistry）
 * 2. 执行状态转换（设置新状态、自动记录时间戳）
 * 3. 生成历史记录快照
 * <p>
 * 不直接操作数据库，由调用方（GreenwayService）负责持久化。
 */
@Component
public class GreenwayStateMachine {

    private static final Logger log = LoggerFactory.getLogger(GreenwayStateMachine.class);

    private final StateTransitionRegistry registry;

    public GreenwayStateMachine(StateTransitionRegistry registry) {
        this.registry = registry;
    }

    /**
     * 校验并执行一次状态转换（内存操作）
     *
     * @param greenway 当前绿道记录
     * @param request  状态变更请求
     * @return 转换结果（新状态 + 是否自动记录了时间戳）
     * @throws BusinessException 当转换非法或校验不通过时
     */
    public TransitionResult transition(StrokeGreenway greenway, StateChangeRequest request) {
        GreenwayStatus fromStatus = parseStatus(greenway.getStatus());
        GreenwayStatus toStatus = parseStatus(request.getTargetStatus());

        // 1. 校验当前状态是否允许该转换
        if (!registry.isValidTransition(fromStatus, toStatus)) {
            throw new BusinessException(400,
                    String.format("非法状态转换: %s → %s", fromStatus.getDisplayName(), toStatus.getDisplayName()));
        }

        // 2. 获取转换规则
        StateTransitionRegistry.TransitionRule rule = registry.getRule(fromStatus, toStatus);
        if (rule == null) {
            throw new BusinessException(500, "状态转换规则未定义");
        }

        // 3. 校验备注（中止/转院/重扫必须填写原因）
        if (rule.isRequireRemark() && (request.getRemark() == null || request.getRemark().isBlank())) {
            throw new BusinessException(400,
                    String.format("转换为「%s」必须填写操作原因", toStatus.getDisplayName()));
        }

        // 4. 校验角色（后续ABAC实现，目前预留）
        if (rule.getRequiredRole() != null) {
            // TODO: 接入 ABAC 权限校验
            log.debug("状态转换需要角色: {}", rule.getRequiredRole());
        }

        // 5. 执行转换（内存中修改对象）
        executeTransition(greenway, toStatus, rule, request);

        return new TransitionResult(fromStatus, toStatus, rule.getAutoTimestampField());
    }

    /**
     * 获取当前状态下所有可用的目标状态
     */
    public Set<GreenwayStatus> getAvailableTargets(GreenwayStatus current) {
        return registry.getAvailableTargets(current);
    }

    /**
     * 获取当前状态下所有可用的目标状态名
     */
    public Set<String> getAvailableTargetNames(GreenwayStatus current) {
        return registry.getAvailableTargetNames(current);
    }

    /**
     * 解析状态枚举（兼容大小写和带前缀的输入）
     */
    public GreenwayStatus parseStatus(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            throw new BusinessException(400, "状态不能为空");
        }
        try {
            return GreenwayStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "未知状态: " + statusStr);
        }
    }

    /**
     * 检查当前状态是否已处于终止态
     */
    public boolean isTerminal(GreenwayStatus status) {
        return status.isTerminal();
    }

    /**
     * 创建历史记录快照
     */
    public StrokeGreenwayHistory createHistory(StrokeGreenway greenway,
                                                GreenwayStatus fromStatus,
                                                GreenwayStatus toStatus,
                                                String operatorId,
                                                String remark) {
        StrokeGreenwayHistory history = new StrokeGreenwayHistory();
        history.setGreenwayId(greenway.getId());
        history.setActionType("STATE_CHANGE");
        history.setFromStatus(fromStatus.name());
        history.setToStatus(toStatus.name());
        history.setOperatorId(operatorId);
        history.setRemark(remark);
        history.setActionTime(LocalDateTime.now());
        history.setSnapshotJson(buildSnapshotJson(greenway));
        return history;
    }

    // ==================== 内部方法 ====================

    private void executeTransition(StrokeGreenway greenway,
                                    GreenwayStatus toStatus,
                                    StateTransitionRegistry.TransitionRule rule,
                                    StateChangeRequest request) {
        // 设置新状态
        greenway.setStatus(toStatus.name());

        // 自动记录时间戳
        if (rule.getAutoTimestampField() != null) {
            LocalDateTime now = LocalDateTime.now();
            switch (rule.getAutoTimestampField()) {
                case "ctOrderTime":
                    if (greenway.getCtOrderTime() == null) {
                        greenway.setCtOrderTime(now);
                    }
                    break;
                case "ctCompleteTime":
                    if (greenway.getCtCompleteTime() == null) {
                        greenway.setCtCompleteTime(now);
                    }
                    break;
                case "needleTime":
                    if (greenway.getNeedleTime() == null) {
                        greenway.setNeedleTime(now);
                    }
                    break;
                default:
                    log.warn("未知的时间戳字段: {}", rule.getAutoTimestampField());
            }
        }

        // 设置决策时间（进入 AWAITING_DECISION 时记录）
        if (toStatus == GreenwayStatus.AWAITING_DECISION && greenway.getDecisionTime() == null) {
            greenway.setDecisionTime(LocalDateTime.now());
        }

        // 设置中止原因
        if (toStatus.isAbnormalTerminal() && request.getRemark() != null) {
            greenway.setAbortReason(request.getRemark());
        }
    }

    private String buildSnapshotJson(StrokeGreenway greenway) {
        // 简单快照：记录关键字段
        return String.format(
                "{\"status\":\"%s\",\"doorTime\":\"%s\",\"ctOrderTime\":\"%s\",\"ctCompleteTime\":\"%s\",\"needleTime\":\"%s\"}",
                Optional.ofNullable(greenway.getStatus()).orElse(""),
                Optional.ofNullable(greenway.getDoorTime()).map(Object::toString).orElse(""),
                Optional.ofNullable(greenway.getCtOrderTime()).map(Object::toString).orElse(""),
                Optional.ofNullable(greenway.getCtCompleteTime()).map(Object::toString).orElse(""),
                Optional.ofNullable(greenway.getNeedleTime()).map(Object::toString).orElse("")
        );
    }

    // ==================== 内部类型 ====================

    /**
     * 转换结果
     */
    public static class TransitionResult {
        private final GreenwayStatus fromStatus;
        private final GreenwayStatus toStatus;
        private final String autoTimestampField;

        public TransitionResult(GreenwayStatus fromStatus, GreenwayStatus toStatus, String autoTimestampField) {
            this.fromStatus = fromStatus;
            this.toStatus = toStatus;
            this.autoTimestampField = autoTimestampField;
        }

        public GreenwayStatus getFromStatus() { return fromStatus; }
        public GreenwayStatus getToStatus() { return toStatus; }
        public String getAutoTimestampField() { return autoTimestampField; }
    }
}
