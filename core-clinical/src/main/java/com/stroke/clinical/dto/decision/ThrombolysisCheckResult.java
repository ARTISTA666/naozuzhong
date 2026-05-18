package com.stroke.clinical.dto.decision;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 溶栓禁忌检查结果
 * <p>
 * 设计文档 3.2.2: "CDS返回禁忌结论，core-clinical立即将状态变更为THROMBOLYSIS_READY并返回前端"
 * 注意：CDS仅为辅助决策，不替代医生判断。
 */
@Schema(description = "溶栓禁忌检查结果")
public class ThrombolysisCheckResult {

    @Schema(description = "最终结论: ELIGIBLE / CAUTION / CONTRAINDICATED", example = "CAUTION")
    private String conclusion;

    @Schema(description = "结论说明", example = "存在相对禁忌症，请医生综合评估")
    private String message;

    @Schema(description = "绝对禁忌计数")
    private int absoluteContraindications;

    @Schema(description = "相对禁忌计数")
    private int relativeContraindications;

    @Schema(description = "各项检查结果明细")
    private List<ContraindicationItem> items;

    /** 是否建议溶栓 */
    private boolean suggested;

    public ThrombolysisCheckResult() {}

    public ThrombolysisCheckResult(String conclusion, String message,
                                    int absoluteCount, int relativeCount,
                                    List<ContraindicationItem> items) {
        this.conclusion = conclusion;
        this.message = message;
        this.absoluteContraindications = absoluteCount;
        this.relativeContraindications = relativeCount;
        this.items = items;
        this.suggested = "ELIGIBLE".equals(conclusion);
    }

    // ====== Getters & Setters ======

    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getAbsoluteContraindications() { return absoluteContraindications; }
    public void setAbsoluteContraindications(int absoluteContraindications) {
        this.absoluteContraindications = absoluteContraindications;
    }

    public int getRelativeContraindications() { return relativeContraindications; }
    public void setRelativeContraindications(int relativeContraindications) {
        this.relativeContraindications = relativeContraindications;
    }

    public List<ContraindicationItem> getItems() { return items; }
    public void setItems(List<ContraindicationItem> items) { this.items = items; }

    public boolean isSuggested() { return suggested; }
    public void setSuggested(boolean suggested) { this.suggested = suggested; }
}
