package com.stroke.rehab.service;

import com.stroke.rehab.entity.FollowupPlan;
import com.stroke.rehab.entity.FollowupTask;
import com.stroke.rehab.entity.PatientReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 随访服务 — 对应设计文档 3.4 节
 * <p>
 * 职责：随访计划生成、任务管理、患者自报数据处理。
 * 与 core-clinical 通过事件异步协同（出院触发随访计划生成）。
 */
@Service
public class FollowupService {

    private static final Logger log = LoggerFactory.getLogger(FollowupService.class);

    /** 默认随访模板：1、3、6、12个月（单位：月） */
    private static final int[] DEFAULT_FOLLOWUP_MONTHS = {1, 3, 6, 12};

    /** 血压异常阈值 */
    private static final int BP_SYSTOLIC_HIGH = 140;
    private static final int BP_DIASTOLIC_HIGH = 90;

    /**
     * 自动生成随访计划（由 EncounterCompleted 事件触发）
     * <p>
     * 对应设计文档：出院时生成1、3、6、12个月随访计划模板，由护士确认。
     *
     * @param patientId   患者ID
     * @param encounterId 就诊ID
     * @param dischargeTime 出院时间
     * @return 生成的随访计划
     */
    public FollowupPlan generateFollowupPlan(Long patientId, Long encounterId, LocalDateTime dischargeTime) {
        FollowupPlan plan = new FollowupPlan();
        plan.setPatientId(patientId);
        plan.setEncounterId(encounterId);
        plan.setPlanName("卒中后随访计划");
        plan.setPlanType("POST_STROKE");
        plan.setStartTime(dischargeTime);
        plan.setStatus("ACTIVE");
        plan.setGenerateMethod("AUTO");

        log.info("自动生成随访计划: patientId={}, encounterId={}", patientId, encounterId);

        // 自动创建随访任务（1、3、6、12个月）
        createDefaultTasks(plan, dischargeTime);

        return plan;
    }

    /**
     * 创建默认随访任务模板
     */
    private List<FollowupTask> createDefaultTasks(FollowupPlan plan, LocalDateTime baseTime) {
        List<FollowupTask> tasks = new ArrayList<>();
        int order = 1;
        for (int months : DEFAULT_FOLLOWUP_MONTHS) {
            FollowupTask task = new FollowupTask();
            task.setPlanId(plan.getId());
            task.setPatientId(plan.getPatientId());
            task.setEncounterId(plan.getEncounterId());
            task.setTaskName(months + "个月随访");
            task.setTaskType("CLINIC_VISIT");
            task.setPlannedDate(baseTime.plusMonths(months).toLocalDate());
            task.setStatus("PENDING");
            task.setSortOrder(order++);
            tasks.add(task);

            log.debug("  生成随访任务: {}个月随访, 计划日期={}", months, task.getPlannedDate());
        }
        // TODO: 持久化任务列表
        return tasks;
    }

    /**
     * 查询患者的随访计划列表
     */
    public List<FollowupPlan> getPlansByPatient(Long patientId) {
        // TODO: 从数据库查询
        log.info("查询患者随访计划: patientId={}", patientId);
        return Collections.emptyList();
    }

    /**
     * 查询随访计划的任务列表
     */
    public List<FollowupTask> getTasksByPlan(Long planId) {
        // TODO: 从数据库查询
        log.info("查询随访任务: planId={}", planId);
        return Collections.emptyList();
    }

    /**
     * 处理患者自报数据（血压/血糖/服药）
     * <p>
     * 对应设计文档：血压连续3次超标或未服药 → 任务列表高亮并通知护士
     */
    public PatientReport processPatientReport(PatientReport report) {
        report.setReportTime(LocalDateTime.now());
        report.setProcessStatus("UNREAD");

        // 异常检测
        if ("BLOOD_PRESSURE".equals(report.getReportType())) {
            checkBloodPressureAbnormal(report);
        } else if ("BLOOD_GLUCOSE".equals(report.getReportType())) {
            checkBloodGlucoseAbnormal(report);
        } else if ("MEDICATION".equals(report.getReportType())) {
            checkMedicationAbnormal(report);
        }

        log.info("处理患者自报数据: patientId={}, type={}, abnormal={}",
                report.getPatientId(), report.getReportType(), report.getAbnormal());

        // TODO: 持久化报告
        return report;
    }

    // ==================== 异常检测 ====================

    private void checkBloodPressureAbnormal(PatientReport report) {
        if (report.getSystolicBp() != null && report.getSystolicBp() > BP_SYSTOLIC_HIGH) {
            report.setAbnormal(true);
            report.setAlertMessage(String.format("收缩压 %d mmHg，高于 %d 阈值",
                    report.getSystolicBp(), BP_SYSTOLIC_HIGH));
        } else if (report.getDiastolicBp() != null && report.getDiastolicBp() > BP_DIASTOLIC_HIGH) {
            report.setAbnormal(true);
            report.setAlertMessage(String.format("舒张压 %d mmHg，高于 %d 阈值",
                    report.getDiastolicBp(), BP_DIASTOLIC_HIGH));
        } else {
            report.setAbnormal(false);
        }
    }

    private void checkBloodGlucoseAbnormal(PatientReport report) {
        if (report.getBloodGlucose() != null) {
            if (report.getBloodGlucose() < 3.9 || report.getBloodGlucose() > 11.1) {
                report.setAbnormal(true);
                report.setAlertMessage(String.format("血糖 %.1f mmol/L，超出正常范围(3.9-11.1)",
                        report.getBloodGlucose()));
            } else {
                report.setAbnormal(false);
            }
        }
    }

    private void checkMedicationAbnormal(PatientReport report) {
        if (Boolean.FALSE.equals(report.getMedicationTaken())) {
            report.setAbnormal(true);
            report.setAlertMessage("患者未按时服药，请及时跟进");
        } else {
            report.setAbnormal(false);
        }
    }
}
