package com.stroke.clinical.dto.decision;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 溶栓禁忌检查请求 — 对应 POST /api/v3/decisions/thrombolysis-check
 * <p>
 * 由前端采集并提交，core-clinical 在服务层同步检查。
 */
@Schema(description = "溶栓禁忌检查请求")
public class ThrombolysisCheckRequest {

    // ==================== 时间窗 ====================

    @NotNull(message = "发病时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发病时间", example = "2026-05-19T08:30:00")
    private LocalDateTime onsetTime;

    // ==================== 检验结果 ====================

    @Schema(description = "INR 国际标准化比值", example = "1.2")
    private Double inr;

    @Schema(description = "血小板计数 (×10⁹/L)", example = "220")
    private Integer plateletCount;

    @Schema(description = "血糖 (mmol/L)", example = "5.6")
    private Double bloodGlucose;

    // ==================== 生命体征 ====================

    @Schema(description = "收缩压 (mmHg)", example = "145")
    private Integer systolicBp;

    @Schema(description = "舒张压 (mmHg)", example = "85")
    private Integer diastolicBp;

    // ==================== 病史 ====================

    @Schema(description = "近期大手术（3周内）", example = "false")
    private Boolean recentMajorSurgery;

    @Schema(description = "颅内出血史", example = "false")
    private Boolean intracranialHemorrhageHistory;

    @Schema(description = "抗凝药物使用", example = "false")
    private Boolean onAnticoagulant;

    // ====== Getters & Setters ======

    public LocalDateTime getOnsetTime() { return onsetTime; }
    public void setOnsetTime(LocalDateTime onsetTime) { this.onsetTime = onsetTime; }

    public Double getInr() { return inr; }
    public void setInr(Double inr) { this.inr = inr; }

    public Integer getPlateletCount() { return plateletCount; }
    public void setPlateletCount(Integer plateletCount) { this.plateletCount = plateletCount; }

    public Double getBloodGlucose() { return bloodGlucose; }
    public void setBloodGlucose(Double bloodGlucose) { this.bloodGlucose = bloodGlucose; }

    public Integer getSystolicBp() { return systolicBp; }
    public void setSystolicBp(Integer systolicBp) { this.systolicBp = systolicBp; }

    public Integer getDiastolicBp() { return diastolicBp; }
    public void setDiastolicBp(Integer diastolicBp) { this.diastolicBp = diastolicBp; }

    public Boolean getRecentMajorSurgery() { return recentMajorSurgery; }
    public void setRecentMajorSurgery(Boolean recentMajorSurgery) { this.recentMajorSurgery = recentMajorSurgery; }

    public Boolean getIntracranialHemorrhageHistory() { return intracranialHemorrhageHistory; }
    public void setIntracranialHemorrhageHistory(Boolean intracranialHemorrhageHistory) {
        this.intracranialHemorrhageHistory = intracranialHemorrhageHistory;
    }

    public Boolean getOnAnticoagulant() { return onAnticoagulant; }
    public void setOnAnticoagulant(Boolean onAnticoagulant) { this.onAnticoagulant = onAnticoagulant; }
}
