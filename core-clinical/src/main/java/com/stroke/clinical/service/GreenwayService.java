package com.stroke.clinical.service;

import com.stroke.clinical.dto.GreenwayVO;
import com.stroke.clinical.dto.StateChangeRequest;
import com.stroke.clinical.event.EventPublisher;
import com.stroke.clinical.repository.mapper.GreenwayHistoryMapper;
import com.stroke.clinical.repository.mapper.GreenwayMapper;
import com.stroke.clinical.service.greenway.GreenwayStateMachine;
import com.stroke.common.BusinessException;
import com.stroke.domain.entity.StrokeGreenway;
import com.stroke.domain.entity.StrokeGreenwayHistory;
import com.stroke.domain.enums.GreenwayStatus;
import com.stroke.domain.event.CtTimeoutWarningEvent;
import com.stroke.domain.event.DntTimeoutWarningEvent;
import cn.hutool.core.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 绿道服务 — 对应设计文档 3.2 节
 * <p>
 * 封装绿道全生命周期管理：创建、状态变更、时间监控。
 */
@Service
public class GreenwayService {

    private static final Logger log = LoggerFactory.getLogger(GreenwayService.class);

    /** DNT红灯阈值（分钟） */
    private static final long DNT_RED_THRESHOLD = 45;

    /** CT完成黄灯阈值（分钟） */
    private static final long CT_YELLOW_THRESHOLD = 25;

    private final GreenwayMapper greenwayMapper;
    private final GreenwayHistoryMapper historyMapper;
    private final GreenwayStateMachine stateMachine;
    private final EventPublisher eventPublisher;

    public GreenwayService(GreenwayMapper greenwayMapper,
                           GreenwayHistoryMapper historyMapper,
                           GreenwayStateMachine stateMachine,
                           EventPublisher eventPublisher) {
        this.greenwayMapper = greenwayMapper;
        this.historyMapper = historyMapper;
        this.stateMachine = stateMachine;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 创建绿道记录（到院登记触发）
     */
    @Transactional
    public StrokeGreenway createGreenway(Long patientId, Long encounterId,
                                          LocalDateTime onsetTime, LocalDateTime doorTime) {
        StrokeGreenway greenway = new StrokeGreenway();
        greenway.setPatientId(patientId);
        greenway.setEncounterId(encounterId);
        greenway.setStatus(GreenwayStatus.WAITING_TRIAGE.name());
        greenway.setOnsetTime(onsetTime);
        greenway.setDoorTime(doorTime);
        greenway.setCreatedBy("SYSTEM");
        greenway.setCreatedTime(LocalDateTime.now());

        greenwayMapper.insert(greenway);
        log.info("创建绿道记录: greenwayId={}, encounterId={}", greenway.getId(), encounterId);
        return greenway;
    }

    /**
     * 绿道状态变更 — 核心方法
     * <p>
     * 使用数据库乐观锁保证并发安全，不依赖Redis锁。
     */
    @Transactional
    public StrokeGreenway changeState(Long encounterId, StateChangeRequest request) {
        // 1. 查询当前绿道记录
        StrokeGreenway greenway = greenwayMapper.findByEncounterId(encounterId);
        if (greenway == null) {
            throw new BusinessException(404, "未找到绿道记录，encounterId=" + encounterId);
        }

        // 2. 校验当前状态是否已终止
        GreenwayStatus currentStatus = stateMachine.parseStatus(greenway.getStatus());
        if (currentStatus.isTerminal()) {
            throw new BusinessException(400, "绿道已处于终止态(" + currentStatus.getDisplayName() + ")，无法继续变更");
        }

        // 3. 校验目标状态与当前是否相同
        if (greenway.getStatus().equals(request.getTargetStatus())) {
            throw new BusinessException(400, "目标状态与当前状态相同，无需变更");
        }

        // 4. 状态机校验并执行转换
        GreenwayStateMachine.TransitionResult result = stateMachine.transition(greenway, request);
        log.info("绿道状态变更: encounterId={}, {} → {}",
                encounterId, result.getFromStatus(), result.getToStatus());

        // 5. 乐观锁更新数据库
        greenway.setUpdatedBy(request.getOperatorId());
        greenway.setUpdatedTime(LocalDateTime.now());
        int updated = greenwayMapper.optimisticUpdate(greenway);
        if (updated == 0) {
            throw new BusinessException(409, "绿道记录已被其他操作修改，请刷新后重试");
        }

        // 6. 写入历史记录
        StrokeGreenwayHistory history = stateMachine.createHistory(
                greenway, result.getFromStatus(), result.getToStatus(),
                request.getOperatorId(), request.getRemark());
        historyMapper.insert(history);

        // 7. 超时检查（触发告警通知）
        checkTimeThresholds(greenway);

        return greenway;
    }

    /**
     * 获取绿道详情（含可用操作列表、DNT时间）
     */
    public GreenwayVO getGreenwayDetail(Long encounterId) {
        StrokeGreenway greenway = greenwayMapper.findByEncounterId(encounterId);
        if (greenway == null) {
            throw new BusinessException(404, "未找到绿道记录，encounterId=" + encounterId);
        }

        GreenwayStatus currentStatus = stateMachine.parseStatus(greenway.getStatus());
        GreenwayVO vo = new GreenwayVO();
        vo.setId(greenway.getId());
        vo.setPatientId(greenway.getPatientId());
        vo.setEncounterId(greenway.getEncounterId());
        vo.setStatus(greenway.getStatus());
        vo.setStatusDisplay(currentStatus.getDisplayName());
        vo.setOnsetTime(greenway.getOnsetTime());
        vo.setDoorTime(greenway.getDoorTime());
        vo.setCtOrderTime(greenway.getCtOrderTime());
        vo.setCtCompleteTime(greenway.getCtCompleteTime());
        vo.setDecisionTime(greenway.getDecisionTime());
        vo.setNeedleTime(greenway.getNeedleTime());
        vo.setContraindications(greenway.getThrombolysisContraindicationsJson());
        vo.setAbortReason(greenway.getAbortReason());

        // 可用操作
        Set<String> availableActions = stateMachine.getAvailableTargetNames(currentStatus);
        vo.setAvailableActions(List.copyOf(availableActions));

        // DNT 时间计算
        if (greenway.getDoorTime() != null && greenway.getNeedleTime() != null) {
            long dntMinutes = Duration.between(greenway.getDoorTime(), greenway.getNeedleTime()).toMinutes();
            vo.setDntMinutes(dntMinutes);
            vo.setDntTimeout(dntMinutes > DNT_RED_THRESHOLD);
        } else if (greenway.getDoorTime() != null) {
            // 尚未溶栓，计算当前DNT
            long dntMinutes = Duration.between(greenway.getDoorTime(), LocalDateTime.now()).toMinutes();
            vo.setDntMinutes(dntMinutes);
            vo.setDntTimeout(dntMinutes > DNT_RED_THRESHOLD);
        }

        return vo;
    }

    /**
     * 获取绿道历史记录
     */
    public List<StrokeGreenwayHistory> getHistory(Long encounterId) {
        StrokeGreenway greenway = greenwayMapper.findByEncounterId(encounterId);
        if (greenway == null) {
            throw new BusinessException(404, "未找到绿道记录");
        }
        return historyMapper.findByGreenwayId(greenway.getId());
    }

    // ==================== 超时监控（设计文档 3.2.3 节） ====================

    private void checkTimeThresholds(StrokeGreenway greenway) {
        LocalDateTime now = LocalDateTime.now();

        // CT 完成超时检查: door_to_ct_complete > 25分钟 → 黄灯
        if (greenway.getDoorTime() != null && greenway.getCtCompleteTime() != null) {
            long ctMinutes = Duration.between(greenway.getDoorTime(), greenway.getCtCompleteTime()).toMinutes();
            if (ctMinutes > CT_YELLOW_THRESHOLD) {
                CtTimeoutWarningEvent event = new CtTimeoutWarningEvent(
                        IdUtil.fastSimpleUUID(),
                        greenway.getEncounterId(),
                        greenway.getPatientId(),
                        ctMinutes,
                        CT_YELLOW_THRESHOLD);
                eventPublisher.publish(event);
                log.warn("【事件发布】CT超时黄灯: encounterId={}, 实际={}分钟",
                        greenway.getEncounterId(), ctMinutes);
            }
        }

        // DNT 超时检查: door_to_needle > 45分钟 → 红灯
        if (greenway.getDoorTime() != null && greenway.getNeedleTime() != null) {
            long dntMinutes = Duration.between(greenway.getDoorTime(), greenway.getNeedleTime()).toMinutes();
            if (dntMinutes > DNT_RED_THRESHOLD) {
                DntTimeoutWarningEvent event = new DntTimeoutWarningEvent(
                        IdUtil.fastSimpleUUID(),
                        greenway.getEncounterId(),
                        greenway.getPatientId(),
                        dntMinutes,
                        DNT_RED_THRESHOLD);
                event.setCurrentStatus(greenway.getStatus());
                eventPublisher.publish(event);
                log.warn("【事件发布】DNT超时红灯: encounterId={}, 实际={}分钟",
                        greenway.getEncounterId(), dntMinutes);
            }
        }
    }
}
