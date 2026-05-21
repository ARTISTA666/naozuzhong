-- ============================================================
-- 脑卒中疾病管理医疗系统 — Docker 初始化脚本
-- 版本: v1.0
-- 说明: 创建数据库 + 业务表 + 测试数据
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS stroke_clinical
    DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS stroke_infra
    DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS stroke_integration
    DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS stroke_rehab
    DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;

-- ==================== stroke_clinical ====================

USE stroke_clinical;

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
    risk_factor_tags VARCHAR(256) COMMENT '卒中危险因素标签',
    status          VARCHAR(16) DEFAULT 'active',
    is_deleted      TINYINT DEFAULT 0, created_by VARCHAR(64) NOT NULL, created_time DATETIME NOT NULL,
    updated_by      VARCHAR(64), updated_time DATETIME, version INT DEFAULT 0, trace_id VARCHAR(64),
    INDEX idx_medical_no (medical_record_no), INDEX idx_id_card (id_card(64)),
    INDEX idx_name (name), INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者主索引';

CREATE TABLE IF NOT EXISTS encounter (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id      BIGINT NOT NULL, department VARCHAR(64),
    encounter_type  VARCHAR(32), arrival_mode VARCHAR(32),
    arrival_time    DATETIME, onset_time DATETIME, last_known_well_time DATETIME,
    stroke_type     VARCHAR(32), hospital_code VARCHAR(32), status VARCHAR(16) DEFAULT 'active',
    is_deleted TINYINT DEFAULT 0, created_by VARCHAR(64) NOT NULL, created_time DATETIME NOT NULL,
    updated_by VARCHAR(64), updated_time DATETIME, version INT DEFAULT 0, trace_id VARCHAR(64),
    INDEX idx_patient (patient_id), INDEX idx_arrival (arrival_time), INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='就诊事件';

CREATE TABLE IF NOT EXISTS stroke_greenway (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id      BIGINT NOT NULL, encounter_id BIGINT NOT NULL,
    status          VARCHAR(32) NOT NULL,
    onset_time DATETIME, door_time DATETIME, ct_order_time DATETIME,
    ct_complete_time DATETIME, decision_time DATETIME, needle_time DATETIME,
    thrombolysis_contraindications_json JSON, abort_reason VARCHAR(128),
    is_deleted TINYINT DEFAULT 0, created_by VARCHAR(64) NOT NULL, created_time DATETIME NOT NULL,
    updated_by VARCHAR(64), updated_time DATETIME, version INT DEFAULT 0, trace_id VARCHAR(64),
    UNIQUE INDEX idx_encounter (encounter_id), INDEX idx_patient (patient_id),
    INDEX idx_status (status), INDEX idx_door_time (door_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绿道记录';

CREATE TABLE IF NOT EXISTS stroke_greenway_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, greenway_id BIGINT NOT NULL,
    action_type VARCHAR(32), from_status VARCHAR(32), to_status VARCHAR(32),
    operator_id VARCHAR(64), remark VARCHAR(256), action_time DATETIME, snapshot_json JSON,
    is_deleted TINYINT DEFAULT 0, created_by VARCHAR(64) NOT NULL, created_time DATETIME NOT NULL,
    updated_by VARCHAR(64), updated_time DATETIME, version INT DEFAULT 0, trace_id VARCHAR(64),
    INDEX idx_greenway (greenway_id), INDEX idx_action_time (action_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绿道历史';

CREATE TABLE IF NOT EXISTS assessment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL, encounter_id BIGINT NOT NULL, greenway_id BIGINT,
    assessment_type VARCHAR(32) NOT NULL, version_no INT,
    assessment_time DATETIME, total_score INT,
    operator_id VARCHAR(64), operator_name VARCHAR(64),
    source VARCHAR(16) DEFAULT 'ONLINE', parent_id BIGINT, record_status VARCHAR(16) DEFAULT 'active',
    is_deleted TINYINT DEFAULT 0, created_by VARCHAR(64) NOT NULL, created_time DATETIME NOT NULL,
    updated_by VARCHAR(64), updated_time DATETIME, version INT DEFAULT 0, trace_id VARCHAR(64),
    INDEX idx_encounter (encounter_id), INDEX idx_patient (patient_id),
    INDEX idx_type (assessment_type), INDEX idx_record_status (record_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估主表';

CREATE TABLE IF NOT EXISTS assessment_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, assessment_id BIGINT NOT NULL,
    item_code VARCHAR(32), item_name VARCHAR(128), score INT, remark VARCHAR(256),
    is_deleted TINYINT DEFAULT 0, created_by VARCHAR(64) NOT NULL, created_time DATETIME NOT NULL,
    updated_by VARCHAR(64), updated_time DATETIME, version INT DEFAULT 0, trace_id VARCHAR(64),
    INDEX idx_assessment (assessment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估明细';

-- 测试数据
INSERT INTO patient (id, name, gender, birth_date, id_card, status, created_by, created_time)
VALUES (1, '张三', 'M', '1990-01-01', '110101199001011234', 'active', 'SYSTEM', NOW()),
       (2, '李四', 'F', '1985-06-15', '11010119850615002X', 'active', 'SYSTEM', NOW()),
       (3, '王五', 'M', '1978-12-20', '11010119781220003X', 'active', 'SYSTEM', NOW());

INSERT INTO encounter (id, patient_id, encounter_type, arrival_mode, arrival_time, onset_time, stroke_type, status, created_by, created_time)
VALUES (1, 1, 'EMERGENCY', 'AMBULANCE', NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 3 HOUR, 'ISCHEMIC', 'active', 'SYSTEM', NOW()),
       (2, 2, 'EMERGENCY', 'WALK_IN', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY - INTERVAL 2 HOUR, 'ISCHEMIC', 'active', 'SYSTEM', NOW());

-- ==================== stroke_infra ====================

USE stroke_infra;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(256) NOT NULL, real_name VARCHAR(64),
    department VARCHAR(64), title VARCHAR(64), role_code VARCHAR(32), role_name VARCHAR(64),
    phone VARCHAR(32), email VARCHAR(128), status VARCHAR(16) DEFAULT 'ACTIVE',
    is_deleted TINYINT DEFAULT 0, created_by VARCHAR(64) NOT NULL, created_time DATETIME NOT NULL,
    updated_by VARCHAR(64), updated_time DATETIME, version INT DEFAULT 0, trace_id VARCHAR(64),
    UNIQUE INDEX idx_username (username), INDEX idx_department (department), INDEX idx_role (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户';

-- 默认管理员账号 (密码: admin123)
INSERT IGNORE INTO sys_user (username, password_hash, real_name, role_code, role_name, department, status, created_by, created_time)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 'ADMIN', '系统管理员', '信息科', 'ACTIVE', 'SYSTEM', NOW()),
       ('doctor1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张医生', 'DOCTOR', '神经内科医生', '神经内科', 'ACTIVE', 'SYSTEM', NOW()),
       ('nurse1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李护士', 'NURSE', '急诊护士', '急诊科', 'ACTIVE', 'SYSTEM', NOW()),
       ('tech1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王技师', 'TECHNICIAN', '放射科技师', '影像科', 'ACTIVE', 'SYSTEM', NOW());

CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_type VARCHAR(32), title VARCHAR(128), content TEXT,
    recipient_id VARCHAR(64), recipient_name VARCHAR(64),
    encounter_id BIGINT, patient_id BIGINT,
    priority VARCHAR(16) DEFAULT 'MEDIUM', channel VARCHAR(32),
    is_read TINYINT DEFAULT 0, read_time DATETIME, send_status VARCHAR(16) DEFAULT 'PENDING',
    is_deleted TINYINT DEFAULT 0, created_by VARCHAR(64) NOT NULL, created_time DATETIME NOT NULL,
    updated_by VARCHAR(64), updated_time DATETIME, version INT DEFAULT 0, trace_id VARCHAR(64),
    INDEX idx_recipient (recipient_id), INDEX idx_type (notification_type),
    INDEX idx_status (send_status), INDEX idx_encounter (encounter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知记录';

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(64), user_name VARCHAR(64), action_type VARCHAR(32),
    target_type VARCHAR(32), target_id VARCHAR(64), detail_json JSON,
    ip_address VARCHAR(64), user_agent VARCHAR(256), action_time DATETIME,
    is_deleted TINYINT DEFAULT 0, created_by VARCHAR(64) NOT NULL, created_time DATETIME NOT NULL,
    updated_by VARCHAR(64), updated_time DATETIME, version INT DEFAULT 0, trace_id VARCHAR(64),
    INDEX idx_user (user_id), INDEX idx_action_time (action_time),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志';

-- 测试用户
INSERT INTO sys_user (username, password_hash, real_name, department, title, role_code, role_name, status, created_by, created_time)
VALUES ('admin', '$2a$10$dummy', '管理员', '神经内科', '主任医师', 'ADMIN', '系统管理员', 'ACTIVE', 'SYSTEM', NOW()),
       ('doctor1', '$2a$10$dummy', '张医生', '神经内科', '主治医师', 'DOCTOR', '医生', 'ACTIVE', 'SYSTEM', NOW()),
       ('nurse1', '$2a$10$dummy', '李护士', '急诊科', '主管护师', 'NURSE', '护士', 'ACTIVE', 'SYSTEM', NOW());

-- ==================== stroke_rehab ====================

USE stroke_rehab;

CREATE TABLE IF NOT EXISTS followup_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL, encounter_id BIGINT NOT NULL,
    plan_name VARCHAR(128), plan_type VARCHAR(32),
    start_time DATETIME, end_time DATETIME, status VARCHAR(16) DEFAULT 'ACTIVE',
    generate_method VARCHAR(16), confirmed_by VARCHAR(64), confirm_time DATETIME, remark VARCHAR(256),
    is_deleted TINYINT DEFAULT 0, created_by VARCHAR(64) NOT NULL, created_time DATETIME NOT NULL,
    updated_by VARCHAR(64), updated_time DATETIME, version INT DEFAULT 0, trace_id VARCHAR(64),
    INDEX idx_patient (patient_id), INDEX idx_encounter (encounter_id), INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='随访计划';

CREATE TABLE IF NOT EXISTS followup_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, plan_id BIGINT,
    patient_id BIGINT NOT NULL, encounter_id BIGINT,
    task_name VARCHAR(128), task_type VARCHAR(32),
    planned_date DATE, completed_time DATETIME, status VARCHAR(16) DEFAULT 'PENDING',
    operator_id VARCHAR(64), operator_name VARCHAR(64), summary TEXT, sort_order INT,
    is_deleted TINYINT DEFAULT 0, created_by VARCHAR(64) NOT NULL, created_time DATETIME NOT NULL,
    updated_by VARCHAR(64), updated_time DATETIME, version INT DEFAULT 0, trace_id VARCHAR(64),
    INDEX idx_plan (plan_id), INDEX idx_patient (patient_id),
    INDEX idx_status (status), INDEX idx_planned_date (planned_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='随访任务';

CREATE TABLE IF NOT EXISTS patient_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, patient_id BIGINT NOT NULL,
    report_type VARCHAR(32), systolic_bp INT, diastolic_bp INT,
    blood_glucose DECIMAL(4,1), medication_taken TINYINT,
    report_time DATETIME, is_abnormal TINYINT DEFAULT 0,
    alert_message VARCHAR(256), process_status VARCHAR(16) DEFAULT 'UNREAD',
    is_deleted TINYINT DEFAULT 0, created_by VARCHAR(64) NOT NULL, created_time DATETIME NOT NULL,
    updated_by VARCHAR(64), updated_time DATETIME, version INT DEFAULT 0, trace_id VARCHAR(64),
    INDEX idx_patient (patient_id), INDEX idx_type (report_type),
    INDEX idx_abnormal (is_abnormal), INDEX idx_report_time (report_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者自报数据';

-- ==================== stroke_integration ====================

USE stroke_integration;

CREATE TABLE IF NOT EXISTS integration_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_system VARCHAR(32), message_type VARCHAR(32), direction VARCHAR(8),
    message_body TEXT, status VARCHAR(16), error_message VARCHAR(512),
    process_time_ms INT, trace_id VARCHAR(64), process_time DATETIME,
    is_deleted TINYINT DEFAULT 0, created_by VARCHAR(64) NOT NULL, created_time DATETIME NOT NULL,
    updated_by VARCHAR(64), updated_time DATETIME, version INT DEFAULT 0,
    INDEX idx_source (source_system), INDEX idx_status (status),
    INDEX idx_process_time (process_time), INDEX idx_trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='集成日志';

CREATE TABLE IF NOT EXISTS mpi_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, global_patient_id BIGINT NOT NULL,
    source_system VARCHAR(32), source_patient_id VARCHAR(64),
    match_rule VARCHAR(32), match_confidence DECIMAL(5,2), status VARCHAR(16) DEFAULT 'ACTIVE',
    is_deleted TINYINT DEFAULT 0, created_by VARCHAR(64) NOT NULL, created_time DATETIME NOT NULL,
    updated_by VARCHAR(64), updated_time DATETIME, version INT DEFAULT 0, trace_id VARCHAR(64),
    INDEX idx_global (global_patient_id), INDEX idx_source (source_system, source_patient_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MPI映射表';
