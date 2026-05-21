package com.stroke.clinical.service.assessment;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * NIHSS 量表配置 — 定义 15 项评分标准的元数据
 * <p>
 * 对应设计文档 3.3 节：量表以配置化表单呈现，版本化管理。
 * <p>
 * NIHSS总分范围: 0-42（分数越高，卒中越严重）
 * - 0: 正常
 * - 1-4: 轻度
 * - 5-15: 中度
 * - 16-20: 中重度
 * - 21-42: 重度
 */
@Component
public class NihssScaleConfig {

    /** 量表版本号 */
    public static final String SCALE_VERSION = "1.0";

    private final Map<String, NihssItemDef> items = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        register("1a", "意识水平 (LOC)", 0, 3,
                "0=清醒，1=嗜睡，2=昏睡，3=昏迷");
        register("1b", "意识水平提问", 0, 2,
                "0=都正确，1=正确一个，2=都不正确");
        register("1c", "意识水平指令", 0, 2,
                "0=都正确，1=正确一个，2=都不正确");
        register("2",  "最佳凝视", 0, 2,
                "0=正常，1=部分凝视麻痹，2=完全凝视麻痹");
        register("3",  "视野", 0, 3,
                "0=无视野缺失，1=部分偏盲，2=完全偏盲，3=双侧偏盲");
        register("4",  "面瘫", 0, 3,
                "0=正常，1=轻度瘫痪，2=部分瘫痪，3=完全瘫痪");
        register("5a", "左上肢运动", 0, 4,
                "0=无下垂，1=下垂不触床，2=下垂触床，3=无抗重力，4=无运动");
        register("5b", "右上肢运动", 0, 4,
                "0=无下垂，1=下垂不触床，2=下垂触床，3=无抗重力，4=无运动");
        register("6a", "左下肢运动", 0, 4,
                "0=无下垂，1=下垂不触床，2=下垂触床，3=无抗重力，4=无运动");
        register("6b", "右下肢运动", 0, 4,
                "0=无下垂，1=下垂不触床，2=下垂触床，3=无抗重力，4=无运动");
        register("7",  "肢体共济失调", 0, 2,
                "0=无，1=一个肢体有，2=两个肢体有");
        register("8",  "感觉", 0, 2,
                "0=正常，1=轻中度感觉丧失，2=重度感觉丧失");
        register("9",  "最佳语言", 0, 3,
                "0=正常，1=轻中度失语，2=重度失语，3=完全失语");
        register("10", "构音障碍", 0, 2,
                "0=正常，1=轻中度构音障碍，2=重度构音障碍");
        register("11", "忽视症", 0, 2,
                "0=正常，1=轻中度忽视，2=重度忽视");
    }

    private void register(String code, String name, int minScore, int maxScore, String description) {
        items.put(code, new NihssItemDef(code, name, minScore, maxScore, description));
    }

    /**
     * 获取所有 NIHSS 条目定义
     */
    public List<NihssItemDef> getAllItems() {
        return List.copyOf(items.values());
    }

    /**
     * 获取指定条目的定义
     */
    public NihssItemDef getItem(String code) {
        return items.get(code);
    }

    /**
     * 验证单项分数是否合法
     */
    public boolean isValidScore(String code, int score) {
        NihssItemDef def = items.get(code);
        if (def == null) return false;
        return score >= def.minScore && score <= def.maxScore;
    }

    /**
     * 验证 NIHSS 总分范围
     */
    public boolean isValidTotalScore(int total) {
        return total >= 0 && total <= 42;
    }

    /**
     * 根据总分获取严重程度
     */
    public String getSeverityLevel(int totalScore) {
        if (totalScore == 0) return "正常";
        if (totalScore <= 4) return "轻度";
        if (totalScore <= 15) return "中度";
        if (totalScore <= 20) return "中重度";
        return "重度";
    }

    /**
     * 验证并计算总分
     *
     * @param scores 条目编码 → 分数
     * @return 计算所得总分
     * @throws IllegalArgumentException 当存在非法分数
     */
    public int validateAndCalculate(Map<String, Integer> scores) {
        int total = 0;
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            String code = entry.getKey();
            Integer score = entry.getValue();
            if (score == null) continue;

            if (!isValidScore(code, score)) {
                NihssItemDef def = items.get(code);
                String msg = (def == null)
                        ? "未知条目: " + code
                        : String.format("条目 %s 分数 %d 无效（允许范围: %d-%d）",
                                code, score, def.minScore, def.maxScore);
                throw new IllegalArgumentException(msg);
            }
            total += score;
        }
        if (!isValidTotalScore(total)) {
            throw new IllegalArgumentException("总分 " + total + " 超出有效范围(0-42)");
        }
        return total;
    }

    // ==================== 内部类型 ====================

    public static class NihssItemDef {
        private final String code;
        private final String name;
        private final int minScore;
        private final int maxScore;
        private final String description;

        public NihssItemDef(String code, String name, int minScore, int maxScore, String description) {
            this.code = code;
            this.name = name;
            this.minScore = minScore;
            this.maxScore = maxScore;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public int getMinScore() { return minScore; }
        public int getMaxScore() { return maxScore; }
        public String getDescription() { return description; }
    }
}
