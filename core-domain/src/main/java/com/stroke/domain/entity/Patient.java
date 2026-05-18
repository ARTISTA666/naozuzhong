package com.stroke.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stroke.common.BaseEntity;

import java.time.LocalDate;

/**
 * 患者主索引 (MPI) — 对应设计文档 3.1 节
 * <p>
 * 通过 integration 服务匹配身份证号、院内ID、区域健康卡号建立映射。
 */
@TableName("patient")
public class Patient extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者姓名 */
    private String name;

    /** 姓名拼音首字母 */
    private String namePinyin;

    /** 性别 (M/F/U) */
    private String gender;

    /** 出生日期 */
    private LocalDate birthDate;

    /** 身份证号（SM4加密存储） */
    private String idCard;

    /** 院内病历号 */
    private String medicalRecordNo;

    /** 联系电话（SM4加密存储） */
    private String phone;

    /** 过敏史 JSON */
    private String allergyJson;

    /** 卒中危险因素标签 JSON（房颤、高血压等） */
    private String riskFactorTags;

    /** 状态: active/merged/inactive */
    private String status;

    // ====== Getters & Setters ======

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNamePinyin() { return namePinyin; }
    public void setNamePinyin(String namePinyin) { this.namePinyin = namePinyin; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }

    public String getMedicalRecordNo() { return medicalRecordNo; }
    public void setMedicalRecordNo(String medicalRecordNo) { this.medicalRecordNo = medicalRecordNo; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAllergyJson() { return allergyJson; }
    public void setAllergyJson(String allergyJson) { this.allergyJson = allergyJson; }

    public String getRiskFactorTags() { return riskFactorTags; }
    public void setRiskFactorTags(String riskFactorTags) { this.riskFactorTags = riskFactorTags; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
