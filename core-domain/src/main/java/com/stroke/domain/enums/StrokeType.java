package com.stroke.domain.enums;

/**
 * 卒中类型
 */
public enum StrokeType {
    ISCHEMIC("缺血性卒中"),
    HEMORRHAGIC("出血性卒中"),
    TIA("短暂性脑缺血发作");

    private final String displayName;

    StrokeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
