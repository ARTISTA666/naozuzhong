package com.stroke.clinical.controller;

import com.stroke.clinical.dto.CreateGreenwayRequest;
import com.stroke.clinical.dto.GreenwayVO;
import com.stroke.clinical.dto.StateChangeRequest;
import com.stroke.clinical.service.GreenwayService;
import com.stroke.common.Result;
import com.stroke.domain.entity.StrokeGreenway;
import com.stroke.domain.entity.StrokeGreenwayHistory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 绿道流程 API — 对应设计文档 5.1 节
 * <p>
 * 核心路径：到院登记 → 状态流转 → 详情查询
 */
@Tag(name = "绿道流程管理")
@RestController
@RequestMapping("/greenway")
public class GreenwayController {

    private final GreenwayService greenwayService;

    public GreenwayController(GreenwayService greenwayService) {
        this.greenwayService = greenwayService;
    }

    @Operation(summary = "创建绿道记录（到院登记）")
    @PostMapping
    public Result<GreenwayVO> createGreenway(
            @Valid @RequestBody CreateGreenwayRequest request) {
        StrokeGreenway greenway = greenwayService.createGreenway(
                request.getPatientId(),
                request.getEncounterId(),
                request.getOnsetTime(),
                request.getDoorTime());
        GreenwayVO vo = greenwayService.getGreenwayDetail(greenway.getEncounterId());
        return Result.success("绿道创建成功", vo);
    }

    @Operation(summary = "绿道状态变更")
    @PostMapping("/{encounterId}/state-change")
    public Result<GreenwayVO> changeState(
            @Parameter(description = "就诊ID") @PathVariable Long encounterId,
            @Valid @RequestBody StateChangeRequest request) {
        StrokeGreenway greenway = greenwayService.changeState(encounterId, request);
        GreenwayVO vo = greenwayService.getGreenwayDetail(greenway.getEncounterId());
        return Result.success("状态变更成功", vo);
    }

    @Operation(summary = "获取绿道详情")
    @GetMapping("/{encounterId}")
    public Result<GreenwayVO> getGreenwayDetail(
            @Parameter(description = "就诊ID") @PathVariable Long encounterId) {
        GreenwayVO vo = greenwayService.getGreenwayDetail(encounterId);
        return Result.success(vo);
    }

    @Operation(summary = "获取状态变更历史")
    @GetMapping("/{encounterId}/history")
    public Result<List<StrokeGreenwayHistory>> getHistory(
            @Parameter(description = "就诊ID") @PathVariable Long encounterId) {
        List<StrokeGreenwayHistory> history = greenwayService.getHistory(encounterId);
        return Result.success(history);
    }
}
