package com.stroke.clinical.service.decision;

import com.stroke.clinical.dto.decision.ContraindicationItem;
import com.stroke.clinical.dto.decision.ThrombolysisCheckRequest;
import com.stroke.clinical.dto.decision.ThrombolysisCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 溶栓禁忌检查服务 — 对应设计文档 3.2.2 节第6步
 * <p>
 * 核心原则：不通过Drools，直接由服务层硬编码计算。
 * 返回建议，不替代医生决策。
 * <p>
 * CDS不可用时，服务内部降级为静态检查（仅INR/时间窗），并提示医生人工复核。
 */
@Service
public class ThrombolysisCheckService {

    private static final Logger log = LoggerFactory.getLogger(ThrombolysisCheckService.class);

    /** 溶栓时间窗：4.5 小时 */
    private static final long THROMBOLYSIS_WINDOW_HOURS = 4;

    /** 溶栓时间窗：4.5 小时（分钟） */
    private static final long THROMBOLYSIS_WINDOW_MINUTES = 270;

    /** 绝对禁忌阈值：INR */
    private static final double INR_ABSOLUTE_THRESHOLD = 1.7;

    /** 绝对禁忌阈值：血小板 (×10⁹/L) */
    private static final int PLATELET_ABSOLUTE_THRESHOLD = 100;

    /** 相对禁忌阈值：收缩压 */
    private static final int SYSTOLIC_BP_THRESHOLD = 185;

    /** 相对禁忌阈值：舒张压 */
    private static final int DIASTOLIC_BP_THRESHOLD = 110;

    /** 低血糖阈值 (mmol/L) */
    private static final double GLUCOSE_LOW_THRESHOLD = 2.7;

    /** 高血糖阈值 (mmol/L) */
    private static final double GLUCOSE_HIGH_THRESHOLD = 22.2;

    /**
     * 执行溶栓禁忌检查
     *
     * @param request 检查请求（由前端采集）
     * @return 检查结果
     */
    public ThrombolysisCheckResult check(ThrombolysisCheckRequest request) {
        List<ContraindicationItem> items = new ArrayList<>();
        int absoluteCount = 0;
        int relativeCount = 0;

        // 运行所有规则
        items.add(checkInr(request));
        items.add(checkPlatelet(request));
        items.add(checkRecentSurgery(request));
        items.add(checkIntracranialHemorrhage(request));
        items.add(checkTimeWindow(request));
        items.add(checkBloodPressure(request));
        items.add(checkBloodGlucose(request));

        // 统计
        for (ContraindicationItem item : items) {
            if (item.isTriggered()) {
                if ("ABSOLUTE".equals(item.getSeverity())) {
                    absoluteCount++;
                } else {
                    relativeCount++;
                }
            }
        }

        // 生成结论
        String conclusion;
        String message;

        if (absoluteCount > 0) {
            conclusion = "CONTRAINDICATED";
            message = "存在" + absoluteCount + "项绝对禁忌症，不建议溶栓。请评估血管内治疗或其他方案。";
        } else if (relativeCount > 0) {
            conclusion = "CAUTION";
            message = "存在" + relativeCount + "项相对禁忌症，请医生综合评估患者获益与风险后决策。";
        } else {
            conclusion = "ELIGIBLE";
            message = "未发现明确禁忌症，建议在时间窗内尽快溶栓。";
        }

        ThrombolysisCheckResult result = new ThrombolysisCheckResult(
                conclusion, message, absoluteCount, relativeCount, items);

        log.info("溶栓禁忌检查完成: conclusion={}, absolute={}, relative={}",
                conclusion, absoluteCount, relativeCount);

        return result;
    }

    // ==================== 各规则实现 ====================

    /**
     * 规则1: INR > 1.7 — 绝对禁忌
     */
    private ContraindicationItem checkInr(ThrombolysisCheckRequest req) {
        if (req.getInr() == null) {
            return new ContraindicationItem("INR_UNKNOWN", "INR评估", "RELATIVE",
                    true, "INR未检测，请尽快完善凝血功能检查", "建议先完成凝血功能检测");
        }
        if (req.getInr() > INR_ABSOLUTE_THRESHOLD) {
            return new ContraindicationItem("INR_HIGH", "INR > 1.7", "ABSOLUTE",
                    true, String.format("INR=%.1f，超过1.7阈值", req.getInr()),
                    "溶栓禁忌，建议评估其他治疗方案");
        }
        return new ContraindicationItem("INR_OK", "INR <= 1.7", "ABSOLUTE",
                false, String.format("INR=%.1f，在安全范围内", req.getInr()), null);
    }

    /**
     * 规则2: 血小板 < 100×10⁹/L — 绝对禁忌
     */
    private ContraindicationItem checkPlatelet(ThrombolysisCheckRequest req) {
        if (req.getPlateletCount() == null) {
            return new ContraindicationItem("PLT_UNKNOWN", "血小板评估", "RELATIVE",
                    true, "血小板未检测，请尽快完善血常规", "建议先完成血常规检测");
        }
        if (req.getPlateletCount() < PLATELET_ABSOLUTE_THRESHOLD) {
            return new ContraindicationItem("PLT_LOW", "血小板 < 100×10⁹/L", "ABSOLUTE",
                    true, String.format("血小板=%d×10⁹/L，低于100阈值", req.getPlateletCount()),
                    "溶栓禁忌，建议评估其他治疗方案");
        }
        return new ContraindicationItem("PLT_OK", "血小板 >= 100×10⁹/L", "ABSOLUTE",
                false, String.format("血小板=%d×10⁹/L，在安全范围内", req.getPlateletCount()), null);
    }

    /**
     * 规则3: 近期大手术（3周内）— 绝对禁忌
     */
    private ContraindicationItem checkRecentSurgery(ThrombolysisCheckRequest req) {
        if (Boolean.TRUE.equals(req.getRecentMajorSurgery())) {
            return new ContraindicationItem("SURGERY_RECENT", "近期大手术（3周内）", "ABSOLUTE",
                    true, "患者3周内有重大手术史",
                    "溶栓出血风险高，建议评估血管内治疗");
        }
        return new ContraindicationItem("SURGERY_OK", "近期无大手术", "ABSOLUTE",
                false, "未报告近期大手术史", null);
    }

    /**
     * 规则4: 颅内出血史 — 绝对禁忌
     */
    private ContraindicationItem checkIntracranialHemorrhage(ThrombolysisCheckRequest req) {
        if (Boolean.TRUE.equals(req.getIntracranialHemorrhageHistory())) {
            return new ContraindicationItem("ICH_HISTORY", "颅内出血史", "ABSOLUTE",
                    true, "患者有颅内出血病史",
                    "溶栓再出血风险极高，建议评估血管内治疗");
        }
        return new ContraindicationItem("ICH_OK", "无颅内出血史", "ABSOLUTE",
                false, "未报告颅内出血史", null);
    }

    /**
     * 规则5: 发病时间 > 4.5小时 — 相对禁忌
     */
    private ContraindicationItem checkTimeWindow(ThrombolysisCheckRequest req) {
        if (req.getOnsetTime() == null) {
            return new ContraindicationItem("TIME_UNKNOWN", "时间窗评估", "RELATIVE",
                    true, "发病时间未明确记录",
                    "请确认发病时间，超4.5h时间窗溶栓获益下降");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(req.getOnsetTime())) {
            return new ContraindicationItem("TIME_INVALID", "发病时间异常", "ABSOLUTE",
                    true, "发病时间晚于当前时间，请核对",
                    "请重新确认发病时间");
        }
        long minutesFromOnset = Duration.between(req.getOnsetTime(), now).toMinutes();
        if (minutesFromOnset > THROMBOLYSIS_WINDOW_MINUTES) {
            return new ContraindicationItem("TIME_EXCEEDED", "超时间窗（>4.5h）", "RELATIVE",
                    true, String.format("发病至现已%d分钟（%.1f小时），超过4.5小时时间窗",
                            minutesFromOnset, minutesFromOnset / 60.0),
                    "超时间窗溶栓获益下降、出血风险增加，请综合影像评估");
        }
        return new ContraindicationItem("TIME_OK", "时间窗内", "RELATIVE",
                false, String.format("发病至现已%d分钟，在4.5小时时间窗内", minutesFromOnset), null);
    }

    /**
     * 规则6: 血压 > 185/110 mmHg — 相对禁忌
     */
    private ContraindicationItem checkBloodPressure(ThrombolysisCheckRequest req) {
        if (req.getSystolicBp() == null || req.getDiastolicBp() == null) {
            return new ContraindicationItem("BP_UNKNOWN", "血压评估", "RELATIVE",
                    true, "血压未测量",
                    "请测量并控制血压后再评估");
        }
        if (req.getSystolicBp() > SYSTOLIC_BP_THRESHOLD
                || req.getDiastolicBp() > DIASTOLIC_BP_THRESHOLD) {
            String detail = String.format("血压 %d/%d mmHg，超过 %d/%d 阈值",
                    req.getSystolicBp(), req.getDiastolicBp(),
                    SYSTOLIC_BP_THRESHOLD, DIASTOLIC_BP_THRESHOLD);
            return new ContraindicationItem("BP_HIGH", "血压 > 185/110 mmHg", "RELATIVE",
                    true, detail,
                    "建议降压治疗后重新评估溶栓指征");
        }
        return new ContraindicationItem("BP_OK", "血压正常", "RELATIVE",
                false, String.format("血压 %d/%d mmHg，在安全范围内",
                        req.getSystolicBp(), req.getDiastolicBp()), null);
    }

    /**
     * 规则7: 血糖 < 2.7 或 > 22.2 mmol/L — 相对禁忌
     */
    private ContraindicationItem checkBloodGlucose(ThrombolysisCheckRequest req) {
        if (req.getBloodGlucose() == null) {
            return new ContraindicationItem("GLU_UNKNOWN", "血糖评估", "RELATIVE",
                    true, "血糖未检测",
                    "建议先完成指尖血糖检测");
        }
        if (req.getBloodGlucose() < GLUCOSE_LOW_THRESHOLD) {
            return new ContraindicationItem("GLU_LOW", "低血糖 < 2.7 mmol/L", "RELATIVE",
                    true, String.format("血糖=%.1f mmol/L，低于2.7", req.getBloodGlucose()),
                    "建议纠正低血糖后重新评估");
        }
        if (req.getBloodGlucose() > GLUCOSE_HIGH_THRESHOLD) {
            return new ContraindicationItem("GLU_HIGH", "高血糖 > 22.2 mmol/L", "RELATIVE",
                    true, String.format("血糖=%.1f mmol/L，高于22.2", req.getBloodGlucose()),
                    "建议控制血糖后重新评估");
        }
        return new ContraindicationItem("GLU_OK", "血糖正常", "RELATIVE",
                false, String.format("血糖=%.1f mmol/L，在安全范围内", req.getBloodGlucose()), null);
    }
}
