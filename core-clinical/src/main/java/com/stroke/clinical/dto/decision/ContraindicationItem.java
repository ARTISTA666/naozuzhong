package com.stroke.clinical.dto.decision;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 单条禁忌检查结果项
 */
@Schema(description = "禁忌检查结果项")
public class ContraindicationItem {

    @Schema(description = "规则编码", example = "INR_HIGH")
    private String ruleCode;

    @Schema(description = "规则名称", example = "INR > 1.7")
    private String ruleName;

    @Schema(description = "严重级别: ABSOLUTE / RELATIVE", example = "ABSOLUTE")
    private String severity;

    @Schema(description = "是否触发", example = "true")
    private boolean triggered;

    @Schema(description = "触发详情", example = "INR=2.1，超过1.7阈值")
    private String detail;

    @Schema(description = "建议", example = "溶栓禁忌，建议评估其他治疗方案")
    private String suggestion;

    public ContraindicationItem() {}

    public ContraindicationItem(String ruleCode, String ruleName, String severity,
                                 boolean triggered, String detail, String suggestion) {
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.severity = severity;
        this.triggered = triggered;
        this.detail = detail;
        this.suggestion = suggestion;
    }

    // ====== Getters & Setters ======

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public boolean isTriggered() { return triggered; }
    public void setTriggered(boolean triggered) { this.triggered = triggered; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
}
