package com.stroke.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stroke.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * 绿道历史版本表 — 对应设计文档 4.2 节
 * <p>
 * 每次更新通过应用层代码写入，保留完整变更历史。
 * 结构同 stroke_greenway，增加操作类型字段。
 */
@TableName("stroke_greenway_history")
public class StrokeGreenwayHistory extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原绿道记录 ID */
    private Long greenwayId;

    /** 操作类型: STATE_CHANGE / ABORT / UPDATE */
    private String actionType;

    /** 变更前状态 */
    private String fromStatus;

    /** 变更后状态 */
    private String toStatus;

    /** 操作人 */
    private String operatorId;

    /** 操作备注 */
    private String remark;

    /** 变更时间 */
    private LocalDateTime actionTime;

    /** 快照: 变更时的完整绿道记录 JSON */
    private String snapshotJson;

    // ====== Getters & Setters ======

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGreenwayId() { return greenwayId; }
    public void setGreenwayId(Long greenwayId) { this.greenwayId = greenwayId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getFromStatus() { return fromStatus; }
    public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }

    public String getToStatus() { return toStatus; }
    public void setToStatus(String toStatus) { this.toStatus = toStatus; }

    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }

    public String getSnapshotJson() { return snapshotJson; }
    public void setSnapshotJson(String snapshotJson) { this.snapshotJson = snapshotJson; }
}
