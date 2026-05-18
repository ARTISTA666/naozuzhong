package com.stroke.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stroke.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * 就诊事件 — 对应设计文档 3.1 节
 * <p>
 * 记录到院方式、发病时间、最后正常时间等关键时间点。
 */
@TableName("encounter")
public class Encounter extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者 ID */
    private Long patientId;

    /** 就诊科室 */
    private String department;

    /** 就诊类型: EMERGENCY / INPATIENT / OUTPATIENT */
    private String encounterType;

    /** 到院方式: AMBULANCE / WALK_IN / TRANSFER */
    private String arrivalMode;

    /** 到院时间 */
    private LocalDateTime arrivalTime;

    /** 发病时间 */
    private LocalDateTime onsetTime;

    /** 最后正常时间 */
    private LocalDateTime lastKnownWellTime;

    /** 卒中类型: ISCHEMIC / HEMORRHAGIC / TIA */
    private String strokeType;

    /** 院区编码（支持多院区） */
    private String hospitalCode;

    /** 就诊状态: active / discharged / transferred */
    private String status;

    // ====== Getters & Setters ======

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getEncounterType() { return encounterType; }
    public void setEncounterType(String encounterType) { this.encounterType = encounterType; }

    public String getArrivalMode() { return arrivalMode; }
    public void setArrivalMode(String arrivalMode) { this.arrivalMode = arrivalMode; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public LocalDateTime getOnsetTime() { return onsetTime; }
    public void setOnsetTime(LocalDateTime onsetTime) { this.onsetTime = onsetTime; }

    public LocalDateTime getLastKnownWellTime() { return lastKnownWellTime; }
    public void setLastKnownWellTime(LocalDateTime lastKnownWellTime) { this.lastKnownWellTime = lastKnownWellTime; }

    public String getStrokeType() { return strokeType; }
    public void setStrokeType(String strokeType) { this.strokeType = strokeType; }

    public String getHospitalCode() { return hospitalCode; }
    public void setHospitalCode(String hospitalCode) { this.hospitalCode = hospitalCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
