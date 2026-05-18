package com.stroke.clinical.dto.assessment;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * NIHSS 单项评分视图
 */
@Schema(description = "NIHSS单项评分")
public class NihssItemVO {

    @Schema(description = "条目编码", example = "1a")
    private String itemCode;

    @Schema(description = "条目名称", example = "意识水平 (LOC)")
    private String itemName;

    @Schema(description = "得分", example = "0")
    private Integer score;

    @Schema(description = "最小分值", example = "0")
    private Integer minScore;

    @Schema(description = "最大分值", example = "3")
    private Integer maxScore;

    @Schema(description = "评分描述", example = "0=清醒，1=嗜睡，2=昏睡，3=昏迷")
    private String description;

    public NihssItemVO() {}

    public NihssItemVO(String itemCode, String itemName, Integer score,
                        Integer minScore, Integer maxScore, String description) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.score = score;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.description = description;
    }

    // ====== Getters & Setters ======

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getMinScore() { return minScore; }
    public void setMinScore(Integer minScore) { this.minScore = minScore; }

    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
