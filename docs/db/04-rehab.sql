-- ============================================================
-- rehab-followup 服务 — 康复随访库
-- 包含: 随访计划、随访任务、患者自报数据
-- 设计文档: 3.4 节
-- ============================================================

USE stroke_rehab;

-- -----------------------------------------------------------
-- 1. 随访计划
-- 对应: com.stroke.rehab.entity.FollowupPlan
-- 说明：出院时自动生成计划模板，护士确认
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS followup_plan (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    patient_id      BIGINT NOT NULL COMMENT '患者ID',
    encounter_id    BIGINT NOT NULL COMMENT '就诊ID',
    plan_name       VARCHAR(128) COMMENT '计划名称',
    plan_type       VARCHAR(32) COMMENT '计划类型: POST_STROKE/TIA/REHAB',
    start_time      DATETIME COMMENT '计划开始日期',
    end_time        DATETIME COMMENT '计划结束日期',
    status          VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/COMPLETED/CANCELLED',
    generate_method VARCHAR(16) COMMENT '生成方式: AUTO/MANUAL',
    confirmed_by    VARCHAR(64) COMMENT '确认护士ID',
    confirm_time    DATETIME COMMENT '确认时间',
    remark          VARCHAR(256) COMMENT '备注',

    is_deleted      TINYINT DEFAULT 0,
    created_by      VARCHAR(64) NOT NULL,
    created_time    DATETIME NOT NULL,
    updated_by      VARCHAR(64),
    updated_time    DATETIME,
    version         INT DEFAULT 0,
    trace_id        VARCHAR(64),

    INDEX idx_patient (patient_id),
    INDEX idx_encounter (encounter_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='随访计划';

-- -----------------------------------------------------------
-- 2. 随访任务
-- 对应: com.stroke.rehab.entity.FollowupTask
-- 说明：由随访计划自动生成，护士确认执行
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS followup_task (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    plan_id         BIGINT COMMENT '随访计划ID',
    patient_id      BIGINT NOT NULL COMMENT '患者ID',
    encounter_id    BIGINT COMMENT '就诊ID',
    task_name       VARCHAR(128) COMMENT '任务名称',
    task_type       VARCHAR(32) COMMENT '任务类型: PHONE_CALL/CLINIC_VISIT/REHAB_ASSESS/MEDICATION_CHECK',
    planned_date    DATE COMMENT '计划日期',
    completed_time  DATETIME COMMENT '实际完成时间',
    status          VARCHAR(16) DEFAULT 'PENDING' COMMENT '状态: PENDING/COMPLETED/SKIPPED/CANCELLED',
    operator_id     VARCHAR(64) COMMENT '执行人ID',
    operator_name   VARCHAR(64) COMMENT '执行人姓名',
    summary         TEXT COMMENT '任务备注/结果摘要',
    sort_order      INT COMMENT '排序号',

    is_deleted      TINYINT DEFAULT 0,
    created_by      VARCHAR(64) NOT NULL,
    created_time    DATETIME NOT NULL,
    updated_by      VARCHAR(64),
    updated_time    DATETIME,
    version         INT DEFAULT 0,
    trace_id        VARCHAR(64),

    INDEX idx_plan (plan_id),
    INDEX idx_patient (patient_id),
    INDEX idx_status (status),
    INDEX idx_planned_date (planned_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='随访任务';

-- -----------------------------------------------------------
-- 3. 患者自报数据
-- 对应: com.stroke.rehab.entity.PatientReport
-- 说明：患者通过小程序上报血压、血糖、服药情况
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS patient_report (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    patient_id      BIGINT NOT NULL COMMENT '患者ID',
    report_type     VARCHAR(32) COMMENT '报告类型: BLOOD_PRESSURE/BLOOD_GLUCOSE/MEDICATION',
    systolic_bp     INT COMMENT '收缩压 mmHg',
    diastolic_bp    INT COMMENT '舒张压 mmHg',
    blood_glucose   DECIMAL(4,1) COMMENT '血糖 mmol/L',
    medication_taken TINYINT COMMENT '是否已服药',
    report_time     DATETIME COMMENT '报告时间',
    is_abnormal     TINYINT DEFAULT 0 COMMENT '是否异常',
    alert_message   VARCHAR(256) COMMENT '预警信息',
    process_status  VARCHAR(16) DEFAULT 'UNREAD' COMMENT '处理状态: UNREAD/READ/PROCESSED',

    is_deleted      TINYINT DEFAULT 0,
    created_by      VARCHAR(64) NOT NULL,
    created_time    DATETIME NOT NULL,
    updated_by      VARCHAR(64),
    updated_time    DATETIME,
    version         INT DEFAULT 0,
    trace_id        VARCHAR(64),

    INDEX idx_patient (patient_id),
    INDEX idx_type (report_type),
    INDEX idx_abnormal (is_abnormal),
    INDEX idx_report_time (report_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者自报数据';
