-- ============================================================
-- integration 服务 — 集成库
-- 包含: 集成日志、MPI映射表
-- 设计文档: 3.5 节
-- ============================================================

USE stroke_integration;

-- -----------------------------------------------------------
-- 1. 集成日志
-- 说明：记录与HIS/LIS/PACS的交互日志
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS integration_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    source_system   VARCHAR(32) COMMENT '来源系统: HIS/LIS/PACS/REGIONAL',
    message_type    VARCHAR(32) COMMENT '消息类型: PATIENT_ADT/ORDER/ RESULT/IMAGE_READY',
    direction       VARCHAR(8) COMMENT '方向: INBOUND/OUTBOUND',
    message_body    TEXT COMMENT '消息体（脱敏后）',
    status          VARCHAR(16) COMMENT '状态: SUCCESS/FAILED/RETRY',
    error_message   VARCHAR(512) COMMENT '错误信息',
    process_time_ms INT COMMENT '处理耗时(ms)',
    trace_id        VARCHAR(64) COMMENT '链路ID',
    process_time    DATETIME COMMENT '处理时间',

    is_deleted      TINYINT DEFAULT 0,
    created_by      VARCHAR(64) NOT NULL,
    created_time    DATETIME NOT NULL,
    updated_by      VARCHAR(64),
    updated_time    DATETIME,
    version         INT DEFAULT 0,

    INDEX idx_source (source_system),
    INDEX idx_status (status),
    INDEX idx_process_time (process_time),
    INDEX idx_trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='集成日志';

-- -----------------------------------------------------------
-- 2. MPI 映射表
-- 说明：患者主索引映射，关联各系统ID
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS mpi_mapping (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    global_patient_id BIGINT NOT NULL COMMENT '全局患者ID',
    source_system   VARCHAR(32) COMMENT '来源系统: HIS/REGIONAL/LIS',
    source_patient_id VARCHAR(64) COMMENT '来源系统患者ID',
    match_rule      VARCHAR(32) COMMENT '匹配规则: ID_CARD/NAME_DOB/ MANUAL',
    match_confidence DECIMAL(5,2) COMMENT '匹配置信度',
    status          VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/MERGED/INACTIVE',

    is_deleted      TINYINT DEFAULT 0,
    created_by      VARCHAR(64) NOT NULL,
    created_time    DATETIME NOT NULL,
    updated_by      VARCHAR(64),
    updated_time    DATETIME,
    version         INT DEFAULT 0,
    trace_id        VARCHAR(64),

    INDEX idx_global (global_patient_id),
    INDEX idx_source (source_system, source_patient_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MPI映射表';
