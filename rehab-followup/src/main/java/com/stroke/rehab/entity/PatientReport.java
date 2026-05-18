package com.stroke.rehab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stroke.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * 患者自报数据 — 对应设计文档 3.4 节
 * <p>
 * 患者通过小程序上报血压、血糖、服药情况。
 * 数据汇总至随访记录，异常时触发预警。
 */
@TableName("patient_report")
public class PatientReport extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 报告类型: BLOOD_PRESSURE / BLOOD_GLUCOSE / MEDICATION */
    private String reportType;

    /** 收缩压 mmHg */
    private Integer systolicBp;

    /** 舒张压 mmHg */
    private Integer diastolicBp;

    /** 血糖 mmol/L */
    private Double bloodGlucose;

    /** 是否已服药 */
    private Boolean medicationTaken;

    /** 报告时间 */
    private LocalDateTime reportTime;

    /** 是否异常标记 */
    private Boolean abnormal;

    /** 预警信息 */
    private String alertMessage;

    /** 处理状态: UNREAD / READ / PROCESSED */
    private String processStatus;

    // ====== Getters & Setters ======

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public Integer getSystolicBp() { return systolicBp; }
    public void setSystolicBp(Integer systolicBp) { this.systolicBp = systolicBp; }

    public Integer getDiastolicBp() { return diastolicBp; }
    public void setDiastolicBp(Integer diastolicBp) { this.diastolicBp = diastolicBp; }

    public Double getBloodGlucose() { return bloodGlucose; }
    public void setBloodGlucose(Double bloodGlucose) { this.bloodGlucose = bloodGlucose; }

    public Boolean getMedicationTaken() { return medicationTaken; }
    public void setMedicationTaken(Boolean medicationTaken) { this.medicationTaken = medicationTaken; }

    public LocalDateTime getReportTime() { return reportTime; }
    public void setReportTime(LocalDateTime reportTime) { this.reportTime = reportTime; }

    public Boolean getAbnormal() { return abnormal; }
    public void setAbnormal(Boolean abnormal) { this.abnormal = abnormal; }

    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }

    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
}
