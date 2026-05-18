package com.stroke.clinical.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 创建绿道请求
 */
@Schema(description = "创建绿道请求")
public class CreateGreenwayRequest {

    @NotNull(message = "患者ID不能为空")
    @Schema(description = "患者ID")
    private Long patientId;

    @NotNull(message = "就诊ID不能为空")
    @Schema(description = "就诊ID")
    private Long encounterId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发病时间")
    private LocalDateTime onsetTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "到院时间不能为空")
    @Schema(description = "到院时间")
    private LocalDateTime doorTime;

    // ====== Getters & Setters ======

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getEncounterId() { return encounterId; }
    public void setEncounterId(Long encounterId) { this.encounterId = encounterId; }

    public LocalDateTime getOnsetTime() { return onsetTime; }
    public void setOnsetTime(LocalDateTime onsetTime) { this.onsetTime = onsetTime; }

    public LocalDateTime getDoorTime() { return doorTime; }
    public void setDoorTime(LocalDateTime doorTime) { this.doorTime = doorTime; }
}
