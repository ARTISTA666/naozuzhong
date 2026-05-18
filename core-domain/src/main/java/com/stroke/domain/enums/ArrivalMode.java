package com.stroke.domain.enums;

/**
 * 到院方式
 */
public enum ArrivalMode {
    AMBULANCE("120急救"),
    WALK_IN("自行来院"),
    TRANSFER("转院转入");

    private final String displayName;

    ArrivalMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
