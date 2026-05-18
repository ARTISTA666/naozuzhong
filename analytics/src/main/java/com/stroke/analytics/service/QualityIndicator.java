package com.stroke.analytics.service;

/**
 * 质控指标定义 — 对应设计文档 3.6 节
 * <p>
 * 看板核心指标枚举。
 * 数据来源：数仓（PostgreSQL），禁止直接从OLTP查询。
 */
public enum QualityIndicator {

    // ====== 时间指标 ======
    DNT_MEDIAN("DNT中位数", "TIME", "入院至溶栓时间中位数", "分钟"),
    DNT_P95("DNT P95", "TIME", "入院至溶栓95分位值", "分钟"),
    DOOR_TO_CT_MEDIAN("Door-to-CT中位数", "TIME", "入院至CT完成中位数", "分钟"),
    OTT_MEDIAN("OTT中位数", "TIME", "发病至治疗中位数", "分钟"),

    // ====== 比率指标 ======
    THROMBOLYSIS_RATE("溶栓率", "RATE", "溶栓患者数/缺血性卒中患者总数", "%"),
    ENDOVASCULAR_RATE("血管内治疗率", "RATE", "取栓患者数/大血管闭塞患者数", "%"),
    CT_TIMEOUT_RATE("CT超时率", "RATE", "Door-to-CT>25min占比", "%"),
    DNT_TIMEOUT_RATE("DNT超时率", "RATE", "Door-to-Needle>45min占比", "%"),

    // ====== 评分指标 ======
    NIHSS_24H_IMPROVEMENT("NIHSS 24h改善率", "SCORE", "24小时后NIHSS评分改善≥4分占比", "%"),
    AVG_NIHSS_ADMISSION("入院平均NIHSS", "SCORE", "入院时平均NIHSS评分", "分"),

    // ====== 流程指标 ======
    GREENWAY_COMPLETION("绿道完成率", "RATE", "进入绿道后完成全流程占比", "%"),
    ABORT_RATE("治疗中止率", "RATE", "绿道中止/总数占比", "%");

    private final String displayName;
    private final String metricType;   // TIME / RATE / SCORE
    private final String description;
    private final String unit;

    QualityIndicator(String displayName, String metricType, String description, String unit) {
        this.displayName = displayName;
        this.metricType = metricType;
        this.description = description;
        this.unit = unit;
    }

    public String getDisplayName() { return displayName; }
    public String getMetricType() { return metricType; }
    public String getDescription() { return description; }
    public String getUnit() { return unit; }

    /**
     * 是否为比率类型指标
     */
    public boolean isRate() {
        return "RATE".equals(metricType);
    }

    /**
     * 是否为时间类型指标
     */
    public boolean isTime() {
        return "TIME".equals(metricType);
    }
}
