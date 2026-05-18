package com.stroke.rehab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stroke.common.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 随访任务 — 对应设计文档 3.4 节
 * <p>
 * 由随访计划自动生成，护士确认执行。
 * 包括：电话随访、门诊复查、康复评定等。
 */
@TableName("followup_task")
public class FollowupTask extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 随访计划ID */
    private Long planId;

    /** 患者ID */
    private Long patientId;

    /** 就诊ID */
    private Long encounterId;

    /** 任务名称 */
    private String taskName;

    /** 任务类型: PHONE_CALL / CLINIC_VISIT / REHAB_ASSESS / MEDICATION_CHECK */
    private String taskType;

    /** 计划日期 */
    private LocalDate plannedDate;

    /** 实际完成日期 */
    private LocalDateTime completedTime;

    /** 状态: PENDING / COMPLETED / SKIPPED / CANCELLED */
    private String status;

    /** 执行人ID */
    private String operatorId;

    /** 执行人姓名 */
    private String operatorName;

    /** 任务备注/结果摘要 */
    private String summary;

    /** 顺序号（同一计划内的排序） */
    private Integer sortOrder;

    // ====== Getters & Setters ======

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getEncounterId() { return encounterId; }
    public void setEncounterId(Long encounterId) { this.encounterId = encounterId; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public LocalDate getPlannedDate() { return plannedDate; }
    public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }

    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
