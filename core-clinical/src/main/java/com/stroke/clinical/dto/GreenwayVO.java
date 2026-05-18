package com.stroke.clinical.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 绿道详情视图对象
 */
@Schema(description = "绿道详情")
public class GreenwayVO {

    @Schema(description = "绿道ID")
    private Long id;

    @Schema(description = "患者ID")
    private Long patientId;

    @Schema(description = "就诊ID")
    private Long encounterId;

    @Schema(description = "当前状态")
    private String status;

    @Schema(description = "当前状态中文名")
    private String statusDisplay;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发病时间")
    private LocalDateTime onsetTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "到院时间")
    private LocalDateTime doorTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "CT开单时间")
    private LocalDateTime ctOrderTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "CT完成时间")
    private LocalDateTime ctCompleteTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "决策时间")
    private LocalDateTime decisionTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "溶栓开始时间")
    private LocalDateTime needleTime;

    @Schema(description = "溶栓禁忌症")
    private String contraindications;

    @Schema(description = "中止原因")
    private String abortReason;

    @Schema(description = "可用操作列表")
    private List<String> availableActions;

    @Schema(description = "DNT已用分钟数")
    private Long dntMinutes;

    @Schema(description = "DNT是否超时")
    private Boolean dntTimeout;

    // ====== Getters & Setters ======

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getEncounterId() { return encounterId; }
    public void setEncounterId(Long encounterId) { this.encounterId = encounterId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }

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

    public String getContraindications() { return contraindications; }
    public void setContraindications(String contraindications) { this.contraindications = contraindications; }

    public String getAbortReason() { return abortReason; }
    public void setAbortReason(String abortReason) { this.abortReason = abortReason; }

    public List<String> getAvailableActions() { return availableActions; }
    public void setAvailableActions(List<String> availableActions) { this.availableActions = availableActions; }

    public Long getDntMinutes() { return dntMinutes; }
    public void setDntMinutes(Long dntMinutes) { this.dntMinutes = dntMinutes; }

    public Boolean getDntTimeout() { return dntTimeout; }
    public void setDntTimeout(Boolean dntTimeout) { this.dntTimeout = dntTimeout; }
}
