package com.stroke.domain.enums;

/**
 * 急诊绿道状态机 — 对应设计文档 3.2.1 节
 * <p>
 * 包含正常路径和异常路径状态。
 */
public enum GreenwayStatus {

    WAITING_TRIAGE("等待分诊"),
    TRIAGING("分诊中"),
    CT_ORDERED("已开CT申请"),
    CT_IN_PROGRESS("CT扫描中"),
    CT_COMPLETED("CT影像可获取"),
    CT_FAILED("CT设备故障或患者无法配合"),
    CT_REPEAT_REQUIRED("影像质量差需重扫"),
    AWAITING_DECISION("等待溶栓/取栓决策"),
    THROMBOLYSIS_READY("决策通过"),
    THROMBOLYSIS_IN_PROGRESS("溶栓执行中"),
    THROMBOLYSIS_COMPLETED("溶栓完成"),
    TREATMENT_ABORTED("治疗中止"),
    TRANSFERRED("转院或转入其他科室"),
    COMPLETED("绿道正常结束");

    private final String displayName;

    GreenwayStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 判断是否为终止态（流程不可继续推进）
     */
    public boolean isTerminal() {
        return this == TREATMENT_ABORTED
                || this == TRANSFERRED
                || this == COMPLETED;
    }

    /**
     * 判断是否为异常终止
     */
    public boolean isAbnormalTerminal() {
        return this == TREATMENT_ABORTED || this == TRANSFERRED;
    }
}
