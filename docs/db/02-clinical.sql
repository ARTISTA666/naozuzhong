-- ============================================================
-- core-clinical 服务 — 核心临床库
-- 包含: 患者主索引、就诊、绿道、评估、评估明细
-- 设计文档: 4.2-4.3 节
-- ============================================================

USE stroke_clinical;

-- -----------------------------------------------------------
-- 1. 患者主索引 (MPI)
-- 对应: com.stroke.domain.entity.Patient
-- 说明：通过integration服务匹配身份证号、院内ID、区域健康卡号
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS patient (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name            VARCHAR(64) NOT NULL COMMENT '患者姓名',
    name_pinyin     VARCHAR(128) COMMENT '姓名拼音首字母',
    gender          VARCHAR(8) COMMENT '性别 (M/F/U)',
    birth_date      DATE COMMENT '出生日期',
    id_card         VARCHAR(256) COMMENT '身份证号（SM4加密存储）',
    medical_record_no VARCHAR(64) COMMENT '院内病历号',
    phone           VARCHAR(256) COMMENT '联系电话（SM4加密存储）',
    allergy_json    TEXT COMMENT '过敏史 JSON',
    risk_factor_tags VARCHAR(256) COMMENT '卒中危险因素标签 JSON（房颤、高血压等）',
    status          VARCHAR(16) DEFAULT 'active' COMMENT '状态: active/merged/inactive',

    -- 审计字段
    is_deleted      TINYINT DEFAULT 0 COMMENT '逻辑删除',
    created_by      VARCHAR(64) NOT NULL COMMENT '创建人',
    created_time    DATETIME NOT NULL COMMENT '创建时间',
    updated_by      VARCHAR(64) COMMENT '更新人',
    updated_time    DATETIME COMMENT '更新时间',
    version         INT DEFAULT 0 COMMENT '乐观锁',
    trace_id        VARCHAR(64) COMMENT '链路ID',

    INDEX idx_medical_no (medical_record_no),
    INDEX idx_id_card (id_card(64)),
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者主索引';

-- -----------------------------------------------------------
-- 2. 就诊事件
-- 对应: com.stroke.domain.entity.Encounter
-- 说明：记录到院方式、发病时间、最后正常时间
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS encounter (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    patient_id      BIGINT NOT NULL COMMENT '患者ID',
    department      VARCHAR(64) COMMENT '就诊科室',
    encounter_type  VARCHAR(32) COMMENT '就诊类型: EMERGENCY/INPATIENT/OUTPATIENT',
    arrival_mode    VARCHAR(32) COMMENT '到院方式: AMBULANCE/WALK_IN/TRANSFER',
    arrival_time    DATETIME COMMENT '到院时间',
    onset_time      DATETIME COMMENT '发病时间',
    last_known_well_time DATETIME COMMENT '最后正常时间',
    stroke_type     VARCHAR(32) COMMENT '卒中类型: ISCHEMIC/HEMORRHAGIC/TIA',
    hospital_code   VARCHAR(32) COMMENT '院区编码',
    status          VARCHAR(16) DEFAULT 'active' COMMENT '就诊状态: active/discharged/transferred',

    is_deleted      TINYINT DEFAULT 0,
    created_by      VARCHAR(64) NOT NULL,
    created_time    DATETIME NOT NULL,
    updated_by      VARCHAR(64),
    updated_time    DATETIME,
    version         INT DEFAULT 0,
    trace_id        VARCHAR(64),

    INDEX idx_patient (patient_id),
    INDEX idx_arrival (arrival_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='就诊事件';

-- -----------------------------------------------------------
-- 3. 绿道记录
-- 对应: com.stroke.domain.entity.StrokeGreenway
-- 说明：绿道状态机核心表，记录救治流程的关键时间和状态
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS stroke_greenway (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    patient_id      BIGINT NOT NULL COMMENT '患者ID',
    encounter_id    BIGINT NOT NULL COMMENT '就诊ID',
    status          VARCHAR(32) NOT NULL COMMENT '绿道状态',
    onset_time      DATETIME COMMENT '发病时间',
    door_time       DATETIME COMMENT '到院时间 (Door Time)',
    ct_order_time   DATETIME COMMENT 'CT开单时间',
    ct_complete_time DATETIME COMMENT 'CT完成时间',
    decision_time   DATETIME COMMENT '决策时间',
    needle_time     DATETIME COMMENT '溶栓开始时间 (Needle Time)',
    thrombolysis_contraindications_json JSON COMMENT '溶栓禁忌症JSON',
    abort_reason    VARCHAR(128) COMMENT '中止原因',

    is_deleted      TINYINT DEFAULT 0,
    created_by      VARCHAR(64) NOT NULL,
    created_time    DATETIME NOT NULL,
    updated_by      VARCHAR(64),
    updated_time    DATETIME,
    version         INT DEFAULT 0,
    trace_id        VARCHAR(64),

    UNIQUE INDEX idx_encounter (encounter_id),
    INDEX idx_patient (patient_id),
    INDEX idx_status (status),
    INDEX idx_door_time (door_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绿道记录';

-- -----------------------------------------------------------
-- 4. 绿道历史版本
-- 对应: com.stroke.domain.entity.StrokeGreenwayHistory
-- 说明：每次状态变更写入，保留完整变更历史
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS stroke_greenway_history (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    greenway_id     BIGINT NOT NULL COMMENT '绿道记录ID',
    action_type     VARCHAR(32) COMMENT '操作类型: STATE_CHANGE/ABORT/UPDATE',
    from_status     VARCHAR(32) COMMENT '变更前状态',
    to_status       VARCHAR(32) COMMENT '变更后状态',
    operator_id     VARCHAR(64) COMMENT '操作人ID',
    remark          VARCHAR(256) COMMENT '操作备注',
    action_time     DATETIME COMMENT '变更时间',
    snapshot_json   JSON COMMENT '变更时快照JSON',

    is_deleted      TINYINT DEFAULT 0,
    created_by      VARCHAR(64) NOT NULL,
    created_time    DATETIME NOT NULL,
    updated_by      VARCHAR(64),
    updated_time    DATETIME,
    version         INT DEFAULT 0,
    trace_id        VARCHAR(64),

    INDEX idx_greenway (greenway_id),
    INDEX idx_action_time (action_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绿道历史版本';

-- -----------------------------------------------------------
-- 5. 评估主表
-- 对应: com.stroke.domain.entity.Assessment
-- 说明：存储评估类型、时间、操作者。修改不覆盖原记录。
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS assessment (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    patient_id      BIGINT NOT NULL COMMENT '患者ID',
    encounter_id    BIGINT NOT NULL COMMENT '就诊ID',
    greenway_id     BIGINT COMMENT '绿道ID',
    assessment_type VARCHAR(32) NOT NULL COMMENT '评估类型: NIHSS/MRS/ASPECTS/GCS',
    version_no      INT COMMENT '评估版本号',
    assessment_time DATETIME COMMENT '评估时间',
    total_score     INT COMMENT '总分',
    operator_id     VARCHAR(64) COMMENT '操作者ID',
    operator_name   VARCHAR(64) COMMENT '操作者姓名',
    source          VARCHAR(16) DEFAULT 'ONLINE' COMMENT '来源: ONLINE/OFFLINE',
    parent_id       BIGINT COMMENT '父评估ID（离线合并时指向原始评估）',
    record_status   VARCHAR(16) DEFAULT 'active' COMMENT '状态: active/superseded/merged',

    is_deleted      TINYINT DEFAULT 0,
    created_by      VARCHAR(64) NOT NULL,
    created_time    DATETIME NOT NULL,
    updated_by      VARCHAR(64),
    updated_time    DATETIME,
    version         INT DEFAULT 0,
    trace_id        VARCHAR(64),

    INDEX idx_encounter (encounter_id),
    INDEX idx_patient (patient_id),
    INDEX idx_type (assessment_type),
    INDEX idx_record_status (record_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估主表';

-- -----------------------------------------------------------
-- 6. 评估明细表
-- 对应: com.stroke.domain.entity.AssessmentItem
-- 说明：存储各条目分数，支持离线上传时标记来源。
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS assessment_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    assessment_id   BIGINT NOT NULL COMMENT '评估主表ID',
    item_code       VARCHAR(32) COMMENT '条目编码（如 NIHSS.1a）',
    item_name       VARCHAR(128) COMMENT '条目名称',
    score           INT COMMENT '条目得分',
    remark          VARCHAR(256) COMMENT '条目备注',

    is_deleted      TINYINT DEFAULT 0,
    created_by      VARCHAR(64) NOT NULL,
    created_time    DATETIME NOT NULL,
    updated_by      VARCHAR(64),
    updated_time    DATETIME,
    version         INT DEFAULT 0,
    trace_id        VARCHAR(64),

    INDEX idx_assessment (assessment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估明细表';
