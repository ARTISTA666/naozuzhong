package com.stroke.domain.enums;

/**
 * 评估量表类型
 */
public enum AssessmentType {
    NIHSS("美国国立卫生研究院卒中量表"),
    MRS("改良Rankin量表"),
    ASPECTS("Alberta卒中项目早期CT评分"),
    GCS("格拉斯哥昏迷评分"),
    FACE_ARM_SPEECH("面臂言语评分(FAST)");

    private final String displayName;

    AssessmentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
