package com.stroke.analytics.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * 数据驾驶舱服务 — 对应设计文档 3.6 节
 * <p>
 * 职责：质控指标计算、看板数据聚合、科研数据导出。
 * <p>
 * 数据来源：数仓（PostgreSQL通过CDC同步），禁止直接查询OLTP。
 * 当前返回模拟数据，接入数仓后替换为真实SQL查询。
 */
@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    /**
     * 获取看板概览（所有质控指标当前值）
     *
     * @param startDate 统计开始日期
     * @param endDate   统计结束日期
     * @return 各指标的值
     */
    public List<IndicatorValue> getDashboardOverview(LocalDate startDate, LocalDate endDate) {
        log.info("查询看板概览: {} ~ {}", startDate, endDate);

        List<IndicatorValue> results = new ArrayList<>();
        for (QualityIndicator indicator : QualityIndicator.values()) {
            results.add(new IndicatorValue(
                    indicator.name(),
                    indicator.getDisplayName(),
                    indicator.getMetricType(),
                    indicator.getUnit(),
                    calculateMockValue(indicator),
                    indicator.getDescription()));
        }
        return results;
    }

    /**
     * 获取 DNT 趋势数据（按天）
     */
    public List<DailyTrend> getDntTrend(LocalDate startDate, LocalDate endDate) {
        log.info("查询DNT趋势: {} ~ {}", startDate, endDate);
        List<DailyTrend> trends = new ArrayList<>();
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            trends.add(new DailyTrend(
                    date,
                    30 + (int)(Math.random() * 20),   // DNT中位数模拟
                    45 + (int)(Math.random() * 30))); // DNT P95模拟
            date = date.plusDays(1);
        }
        return trends;
    }

    /**
     * 获取溶栓率按月统计
     */
    public List<MonthlyRate> getMonthlyThrombolysisRate(int year) {
        log.info("查询溶栓率: year={}", year);
        List<MonthlyRate> rates = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            rates.add(new MonthlyRate(
                    year, month,
                    60 + Math.random() * 30,  // 溶栓率 60-90%
                    5 + Math.random() * 15)); // DNT超时率 5-20%
        }
        return rates;
    }

    /**
     * 获取质控指标定义列表（不含值，用于配置展示）
     */
    public List<IndicatorMeta> getIndicatorDefinitions() {
        List<IndicatorMeta> metas = new ArrayList<>();
        for (QualityIndicator indicator : QualityIndicator.values()) {
            metas.add(new IndicatorMeta(
                    indicator.name(),
                    indicator.getDisplayName(),
                    indicator.getMetricType(),
                    indicator.getUnit(),
                    indicator.getDescription()));
        }
        return metas;
    }

    // ==================== 模拟数据（接入数仓后替换） ====================

    private double calculateMockValue(QualityIndicator indicator) {
        switch (indicator) {
            case DNT_MEDIAN:            return 38 + Math.random() * 10;   // 38-48 min
            case DNT_P95:               return 55 + Math.random() * 20;  // 55-75 min
            case DOOR_TO_CT_MEDIAN:     return 18 + Math.random() * 10;  // 18-28 min
            case OTT_MEDIAN:            return 120 + Math.random() * 60; // 120-180 min
            case THROMBOLYSIS_RATE:     return 65 + Math.random() * 25;  // 65-90%
            case ENDOVASCULAR_RATE:     return 40 + Math.random() * 30;  // 40-70%
            case CT_TIMEOUT_RATE:       return 5 + Math.random() * 15;   // 5-20%
            case DNT_TIMEOUT_RATE:      return 8 + Math.random() * 12;   // 8-20%
            case NIHSS_24H_IMPROVEMENT: return 45 + Math.random() * 20;  // 45-65%
            case AVG_NIHSS_ADMISSION:   return 8 + Math.random() * 6;    // 8-14
            case GREENWAY_COMPLETION:   return 75 + Math.random() * 15;  // 75-90%
            case ABORT_RATE:            return 2 + Math.random() * 5;    // 2-7%
            default:                    return 0;
        }
    }

    // ==================== 内部类型 ====================

    /**
     * 指标值（含当前值）
     */
    public static class IndicatorValue {
        private final String code;
        private final String displayName;
        private final String metricType;
        private final String unit;
        private final double value;
        private final String description;

        public IndicatorValue(String code, String displayName, String metricType,
                              String unit, double value, String description) {
            this.code = code;
            this.displayName = displayName;
            this.metricType = metricType;
            this.unit = unit;
            this.value = Math.round(value * 10.0) / 10.0;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDisplayName() { return displayName; }
        public String getMetricType() { return metricType; }
        public String getUnit() { return unit; }
        public double getValue() { return value; }
        public String getDescription() { return description; }
    }

    /**
     * 每日趋势
     */
    public static class DailyTrend {
        private final LocalDate date;
        private final double dntMedian;
        private final double dntP95;

        public DailyTrend(LocalDate date, double dntMedian, double dntP95) {
            this.date = date;
            this.dntMedian = dntMedian;
            this.dntP95 = dntP95;
        }

        public LocalDate getDate() { return date; }
        public double getDntMedian() { return dntMedian; }
        public double getDntP95() { return dntP95; }
    }

    /**
     * 月统计
     */
    public static class MonthlyRate {
        private final int year;
        private final int month;
        private final double thrombolysisRate;
        private final double dntTimeoutRate;

        public MonthlyRate(int year, int month, double thrombolysisRate, double dntTimeoutRate) {
            this.year = year;
            this.month = month;
            this.thrombolysisRate = Math.round(thrombolysisRate * 10.0) / 10.0;
            this.dntTimeoutRate = Math.round(dntTimeoutRate * 10.0) / 10.0;
        }

        public int getYear() { return year; }
        public int getMonth() { return month; }
        public double getThrombolysisRate() { return thrombolysisRate; }
        public double getDntTimeoutRate() { return dntTimeoutRate; }
    }

    /**
     * 指标元数据（不含值）
     */
    public static class IndicatorMeta {
        private final String code;
        private final String displayName;
        private final String metricType;
        private final String unit;
        private final String description;

        public IndicatorMeta(String code, String displayName, String metricType,
                             String unit, String description) {
            this.code = code;
            this.displayName = displayName;
            this.metricType = metricType;
            this.unit = unit;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDisplayName() { return displayName; }
        public String getMetricType() { return metricType; }
        public String getUnit() { return unit; }
        public String getDescription() { return description; }
    }
}
