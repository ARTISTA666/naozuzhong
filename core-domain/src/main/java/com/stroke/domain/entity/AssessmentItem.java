package com.stroke.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stroke.common.BaseEntity;

/**
 * 评估明细表 — 对应设计文档 4.3 节
 * <p>
 * 存储各条目分数，支持离线上传时标记来源。
 */
@TableName("assessment_item")
public class AssessmentItem extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评估主表 ID */
    private Long assessmentId;

    /** 条目编码（如 NIHSS.1a, NIHSS.1b） */
    private String itemCode;

    /** 条目名称 */
    private String itemName;

    /** 条目得分 */
    private Integer score;

    /** 条目备注 */
    private String remark;

    // ====== Getters & Setters ======

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
