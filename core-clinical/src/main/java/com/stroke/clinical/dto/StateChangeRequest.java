package com.stroke.clinical.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 绿道状态变更请求 — 对应 POST /api/v3/greenway/{encounterId}/state-change
 */
@Schema(description = "绿道状态变更请求")
public class StateChangeRequest {

    @NotNull(message = "目标状态不能为空")
    @Schema(description = "目标状态", example = "CT_ORDERED")
    private String targetStatus;

    @Schema(description = "操作备注（中止/转院时必须填写原因）", example = "患者家属拒绝溶栓")
    private String remark;

    @Schema(description = "操作人ID", hidden = true)
    private String operatorId;

    @Schema(description = "操作人姓名", hidden = true)
    private String operatorName;

    // ====== Getters & Setters ======

    public String getTargetStatus() { return targetStatus; }
    public void setTargetStatus(String targetStatus) { this.targetStatus = targetStatus; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
}
