package com.stroke.rehab.controller;

import com.stroke.common.Result;
import com.stroke.rehab.entity.FollowupPlan;
import com.stroke.rehab.entity.FollowupTask;
import com.stroke.rehab.entity.PatientReport;
import com.stroke.rehab.service.FollowupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 随访管理 API — 对应设计文档 3.4 节
 */
@Tag(name = "随访管理")
@RestController
@RequestMapping("/followup")
public class FollowupController {

    private final FollowupService followupService;

    public FollowupController(FollowupService followupService) {
        this.followupService = followupService;
    }

    @Operation(summary = "获取患者随访计划列表")
    @GetMapping("/plans")
    public Result<List<FollowupPlan>> getPlans(
            @Parameter(description = "患者ID") @RequestParam Long patientId) {
        List<FollowupPlan> plans = followupService.getPlansByPatient(patientId);
        return Result.success(plans);
    }

    @Operation(summary = "获取随访任务列表")
    @GetMapping("/tasks")
    public Result<List<FollowupTask>> getTasks(
            @Parameter(description = "随访计划ID") @RequestParam Long planId) {
        List<FollowupTask> tasks = followupService.getTasksByPlan(planId);
        return Result.success(tasks);
    }

    @Operation(summary = "提交患者自报数据")
    @PostMapping("/reports")
    public Result<PatientReport> submitReport(@RequestBody PatientReport report) {
        PatientReport processed = followupService.processPatientReport(report);
        return Result.success("数据提交成功", processed);
    }
}
