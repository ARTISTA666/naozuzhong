package com.stroke.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stroke.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * 评估主表 — 对应设计文档 4.3 节及 3.3 节
 * <p>
 * 存储评估类型、时间、操作者。
 * 支持离线上传时标记来源。修改不更新原行，而是插入新行并递增版本号。
 */
@TableName("assessment")
public class Assessment extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者 ID */
    private Long patientId;

    /** 就诊 ID */
    private Long encounterId;

    /** 绿道 ID */
    private Long greenwayId;

    /** 评估类型 — NIHSS / MRS / ASPECTS / GCS / FACE_ARM_SPEECH */
    private String assessmentType;

    /** 评估版本号 */
    private Integer versionNo;

    /** 评估时间 */
    private LocalDateTime assessmentTime;

    /** 总分 */
    private Integer totalScore;

    /** 操作者 ID */
    private String operatorId;

    /** 操作者姓名 */
    private String operatorName;

    /** 来源: ONLINE / OFFLINE */
    private String source;

    /** 父评估ID（离线数据合并时指向原始评估） */
    private Long parentId;

    /** 状态: active / superseded / merged */
    private String recordStatus;

    // ====== Getters & Setters ======

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getEncounterId() { return encounterId; }
    public void setEncounterId(Long encounterId) { this.encounterId = encounterId; }

    public Long getGreenwayId() { return greenwayId; }
    public void setGreenwayId(Long greenwayId) { this.greenwayId = greenwayId; }

    public String getAssessmentType() { return assessmentType; }
    public void setAssessmentType(String assessmentType) { this.assessmentType = assessmentType; }

    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }

    public LocalDateTime getAssessmentTime() { return assessmentTime; }
    public void setAssessmentTime(LocalDateTime assessmentTime) { this.assessmentTime = assessmentTime; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getRecordStatus() { return recordStatus; }
    public void setRecordStatus(String recordStatus) { this.recordStatus = recordStatus; }
}
