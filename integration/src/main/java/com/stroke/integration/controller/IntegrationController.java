package com.stroke.integration.controller;

import com.stroke.common.Result;
import com.stroke.integration.service.DataStandardizationService;
import com.stroke.integration.service.MpiMatchService;
import com.stroke.integration.service.fhir.FhirGatewayService;
import com.stroke.integration.service.hl7.Hl7MessageHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 集成互联互通 API — 对应设计文档 3.5 节
 * <p>
 * 职责：对接HIS/LIS/PACS，手动录入降级，MPI匹配。
 */
@Tag(name = "集成互联互通")
@RestController
@RequestMapping("/integration")
public class IntegrationController {

    private final Hl7MessageHandler hl7Handler;
    private final FhirGatewayService fhirGateway;
    private final MpiMatchService mpiMatchService;
    private final DataStandardizationService dataStdService;

    public IntegrationController(Hl7MessageHandler hl7Handler,
                                 FhirGatewayService fhirGateway,
                                 MpiMatchService mpiMatchService,
                                 DataStandardizationService dataStdService) {
        this.hl7Handler = hl7Handler;
        this.fhirGateway = fhirGateway;
        this.mpiMatchService = mpiMatchService;
        this.dataStdService = dataStdService;
    }

    @Operation(summary = "接收 HL7 v2 消息",
            description = "从 HIS 接收 HL7 v2 消息（ADT/ORM），解析后转发至核心业务服务。")
    @PostMapping("/hl7")
    public Result<Hl7MessageHandler.Hl7Result> receiveHl7(
            @RequestBody String hl7Message) {
        Hl7MessageHandler.Hl7Result result = hl7Handler.handleMessage(hl7Message);
        if (result.isSuccess()) {
            return Result.success(result);
        }
        return Result.badRequest(result.getMessage());
    }

    @Operation(summary = "接收 FHIR 资源",
            description = "从 PACS/区域平台接收 FHIR R4 资源。")
    @PostMapping("/fhir/{resourceType}")
    public Result<FhirGatewayService.FhirResult> receiveFhir(
            @PathVariable String resourceType,
            @RequestBody String fhirJson) {
        FhirGatewayService.FhirResult result = fhirGateway.handleResource(resourceType, fhirJson);
        if (result.isSuccess()) {
            return Result.success(result);
        }
        return Result.badRequest(result.getMessage());
    }

    @Operation(summary = "患者匹配",
            description = "根据身份证号/姓名+出生日期执行患者主索引匹配。")
    @PostMapping("/patient-match")
    public Result<MpiMatchService.MatchResult> matchPatient(
            @RequestBody Map<String, Object> request) {
        String idCard = (String) request.get("idCard");
        String name = (String) request.get("name");
        String birthDateStr = (String) request.get("birthDate");
        String gender = (String) request.get("gender");

        LocalDate birthDate = birthDateStr != null ? LocalDate.parse(birthDateStr) : null;

        // 性别标准化
        String stdGender = dataStdService.standardizeGender(gender);

        MpiMatchService.MatchResult result = mpiMatchService.matchPatient(idCard, name, birthDate, stdGender);
        return Result.success(result);
    }

    @Operation(summary = "数据标准化测试",
            description = "测试性别/时间格式标准化。")
    @PostMapping("/standardize/gender")
    public Result<String> standardizeGender(@RequestBody Map<String, String> request) {
        String result = dataStdService.standardizeGender(request.get("gender"));
        return Result.success(result);
    }

    @Operation(summary = "生成质控 FHIR Bundle",
            description = "生成指定月份质控数据的FHIR Bundle输出。")
    @GetMapping("/quality-bundle")
    public Result<String> getQualityBundle(
            @RequestParam(required = false) String hospitalCode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        if (hospitalCode == null) hospitalCode = "HOSP001";
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        String bundle = fhirGateway.generateQualityBundle(hospitalCode, year, month);
        return Result.success(bundle);
    }
}
