package com.stroke.clinical.dto.assessment;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NIHSS 评估结果视图
 */
@Schema(description = "NIHSS评估结果")
public class NihssAssessmentVO {

    @Schema(description = "评估ID")
    private Long assessmentId;

    @Schema(description = "就诊ID")
    private Long encounterId;

    @Schema(description = "患者ID")
    private Long patientId;

    @Schema(description = "评估版本号")
    private Integer versionNo;

    @Schema(description = "总分", example = "6")
    private Integer totalScore;

    @Schema(description = "严重程度", example = "中度")
    private String severityLevel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "评估时间")
    private LocalDateTime assessmentTime;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "来源: ONLINE / OFFLINE")
    private String source;

    @Schema(description = "记录状态: active / superseded / merged")
    private String recordStatus;

    @Schema(description = "各项评分明细")
    private List<NihssItemVO> items;

    // ====== Getters & Setters ======

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public Long getEncounterId() { return encounterId; }
    public void setEncounterId(Long encounterId) { this.encounterId = encounterId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) { this.severityLevel = severityLevel; }

    public LocalDateTime getAssessmentTime() { return assessmentTime; }
    public void setAssessmentTime(LocalDateTime assessmentTime) { this.assessmentTime = assessmentTime; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getRecordStatus() { return recordStatus; }
    public void setRecordStatus(String recordStatus) { this.recordStatus = recordStatus; }

    public List<NihssItemVO> getItems() { return items; }
    public void setItems(List<NihssItemVO> items) { this.items = items; }
}
