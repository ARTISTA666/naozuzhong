package com.stroke.clinical.controller;

import com.stroke.clinical.dto.decision.ThrombolysisCheckRequest;
import com.stroke.clinical.dto.decision.ThrombolysisCheckResult;
import com.stroke.clinical.service.decision.ThrombolysisCheckService;
import com.stroke.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 临床决策支持 API — 对应设计文档 5.1 节
 * <p>
 * 前端调用同步API，core-clinical内部直接计算，不走异步。
 */
@Tag(name = "临床决策支持 (CDS)")
@RestController
@RequestMapping("/decisions")
public class DecisionController {

    private final ThrombolysisCheckService checkService;

    public DecisionController(ThrombolysisCheckService checkService) {
        this.checkService = checkService;
    }

    @Operation(summary = "溶栓禁忌同步检查",
            description = "前端提交患者临床数据，服务层同步检查禁忌症。"
                    + "不通过Drools，由服务层硬编码计算。返回建议，不替代医生决策。")
    @PostMapping("/thrombolysis-check")
    public Result<ThrombolysisCheckResult> checkThrombolysis(
            @Valid @RequestBody ThrombolysisCheckRequest request) {
        ThrombolysisCheckResult result = checkService.check(request);
        return Result.success(result);
    }
}
