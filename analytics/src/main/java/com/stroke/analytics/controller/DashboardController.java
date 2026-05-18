package com.stroke.analytics.controller;

import com.stroke.analytics.service.AnalyticsService;
import com.stroke.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 数据驾驶舱 API — 对应设计文档 3.6 节
 * <p>
 * 所有查询指向数仓（PostgreSQL），禁止OLTP库复杂查询。
 */
@Tag(name = "数据驾驶舱")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final AnalyticsService analyticsService;

    public DashboardController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "看板概览", description = "返回所有质控指标的当前值")
    @GetMapping("/overview")
    public Result<List<AnalyticsService.IndicatorValue>> getOverview(
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();
        return Result.success(analyticsService.getDashboardOverview(startDate, endDate));
    }

    @Operation(summary = "DNT趋势", description = "按天返回DNT中位数和P95")
    @GetMapping("/dnt-trend")
    public Result<List<AnalyticsService.DailyTrend>> getDntTrend(
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusWeeks(2);
        if (endDate == null) endDate = LocalDate.now();
        return Result.success(analyticsService.getDntTrend(startDate, endDate));
    }

    @Operation(summary = "月溶栓率", description = "按月份返回溶栓率和DNT超时率")
    @GetMapping("/thrombolysis-rate")
    public Result<List<AnalyticsService.MonthlyRate>> getMonthlyRate(
            @Parameter(description = "年份") @RequestParam(required = false) Integer year) {
        if (year == null) year = LocalDate.now().getYear();
        return Result.success(analyticsService.getMonthlyThrombolysisRate(year));
    }

    @Operation(summary = "指标定义", description = "返回所有质控指标的元数据定义")
    @GetMapping("/indicators")
    public Result<List<AnalyticsService.IndicatorMeta>> getIndicators() {
        return Result.success(analyticsService.getIndicatorDefinitions());
    }
}
