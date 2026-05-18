package com.stroke.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stroke.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * 急诊绿道记录 — 对应设计文档 4.2 节及 3.2 节
 * <p>
 * 绿道状态机核心表，记录整个救治流程的关键时间和状态。
 */
@TableName("stroke_greenway")
public class StrokeGreenway extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者 ID */
    private Long patientId;

    /** 就诊 ID */
    private Long encounterId;

    /** 绿道状态 — 对应 GreenwayStatus 枚举值 */
    private String status;

    /** 发病时间 */
    private LocalDateTime onsetTime;

    /** 到院时间 (Door Time) */
    private LocalDateTime doorTime;

    /** CT开单时间 */
    private LocalDateTime ctOrderTime;

    /** CT完成时间 */
    private LocalDateTime ctCompleteTime;

    /** 决策时间 */
    private LocalDateTime decisionTime;

    /** 溶栓开始时间 (Needle Time) */
    private LocalDateTime needleTime;

    /** 溶栓禁忌症 JSON */
    private String thrombolysisContraindicationsJson;

    /** 中止原因 */
    private String abortReason;

    // ====== Getters & Setters ======

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getEncounterId() { return encounterId; }
    public void setEncounterId(Long encounterId) { this.encounterId = encounterId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getOnsetTime() { return onsetTime; }
    public void setOnsetTime(LocalDateTime onsetTime) { this.onsetTime = onsetTime; }

    public LocalDateTime getDoorTime() { return doorTime; }
    public void setDoorTime(LocalDateTime doorTime) { this.doorTime = doorTime; }

    public LocalDateTime getCtOrderTime() { return ctOrderTime; }
    public void setCtOrderTime(LocalDateTime ctOrderTime) { this.ctOrderTime = ctOrderTime; }

    public LocalDateTime getCtCompleteTime() { return ctCompleteTime; }
    public void setCtCompleteTime(LocalDateTime ctCompleteTime) { this.ctCompleteTime = ctCompleteTime; }

    public LocalDateTime getDecisionTime() { return decisionTime; }
    public void setDecisionTime(LocalDateTime decisionTime) { this.decisionTime = decisionTime; }

    public LocalDateTime getNeedleTime() { return needleTime; }
    public void setNeedleTime(LocalDateTime needleTime) { this.needleTime = needleTime; }

    public String getThrombolysisContraindicationsJson() { return thrombolysisContraindicationsJson; }
    public void setThrombolysisContraindicationsJson(String thrombolysisContraindicationsJson) {
        this.thrombolysisContraindicationsJson = thrombolysisContraindicationsJson;
    }

    public String getAbortReason() { return abortReason; }
    public void setAbortReason(String abortReason) { this.abortReason = abortReason; }
}
