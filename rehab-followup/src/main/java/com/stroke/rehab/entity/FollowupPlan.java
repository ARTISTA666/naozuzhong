package com.stroke.rehab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stroke.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * 随访计划 — 对应设计文档 3.4 节
 * <p>
 * 出院时由 EncounterCompleted 事件触发自动生成计划模板。
 * 默认模板节点：1个月、3个月、6个月、12个月。
 */
@TableName("followup_plan")
public class FollowupPlan extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 就诊ID */
    private Long encounterId;

    /** 计划名称 */
    private String planName;

    /** 计划类型: POST_STROKE / TIA / REHAB */
    private String planType;

    /** 计划开始日期 */
    private LocalDateTime startTime;

    /** 计划结束日期 */
    private LocalDateTime endTime;

    /** 状态: ACTIVE / COMPLETED / CANCELLED */
    private String status;

    /** 计划生成方式: AUTO（系统自动）/ MANUAL（人工创建） */
    private String generateMethod;

    /** 确认护士ID */
    private String confirmedBy;

    /** 确认时间 */
    private LocalDateTime confirmTime;

    /** 备注 */
    private String remark;

    // ====== Getters & Setters ======

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getEncounterId() { return encounterId; }
    public void setEncounterId(Long encounterId) { this.encounterId = encounterId; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGenerateMethod() { return generateMethod; }
    public void setGenerateMethod(String generateMethod) { this.generateMethod = generateMethod; }

    public String getConfirmedBy() { return confirmedBy; }
    public void setConfirmedBy(String confirmedBy) { this.confirmedBy = confirmedBy; }

    public LocalDateTime getConfirmTime() { return confirmTime; }
    public void setConfirmTime(LocalDateTime confirmTime) { this.confirmTime = confirmTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
