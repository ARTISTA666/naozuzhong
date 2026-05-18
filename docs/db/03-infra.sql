-- ============================================================
-- infrastructure 服务 — 基础设施库
-- 包含: 用户表、通知表、审计日志
-- 设计文档: 3.7 节
-- ============================================================

USE stroke_infra;

-- -----------------------------------------------------------
-- 1. 用户表（系统管理）
-- 说明：RBAC为基础，补充ABAC属性
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username        VARCHAR(64) NOT NULL COMMENT '登录名',
    password_hash   VARCHAR(256) NOT NULL COMMENT '密码哈希',
    real_name       VARCHAR(64) COMMENT '真实姓名',
    department      VARCHAR(64) COMMENT '科室',
    title           VARCHAR(64) COMMENT '职称',
    role_code       VARCHAR(32) COMMENT '角色编码',
    role_name       VARCHAR(64) COMMENT '角色名称',
    phone           VARCHAR(32) COMMENT '手机号',
    email           VARCHAR(128) COMMENT '邮箱',
    status          VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/LOCKED/DISABLED',

    is_deleted      TINYINT DEFAULT 0,
    created_by      VARCHAR(64) NOT NULL,
    created_time    DATETIME NOT NULL,
    updated_by      VARCHAR(64),
    updated_time    DATETIME,
    version         INT DEFAULT 0,
    trace_id        VARCHAR(64),

    UNIQUE INDEX idx_username (username),
    INDEX idx_department (department),
    INDEX idx_role (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户';

-- -----------------------------------------------------------
-- 2. 通知记录表（站内信）
-- 对应: com.stroke.infra.entity.Notification
-- 说明：DNT超时告警、CT超时提醒、溶栓通知等
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS notification (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    notification_type VARCHAR(32) COMMENT '通知类型: DNT_TIMEOUT/CT_TIMEOUT/THROMBOLYSIS_READY/GENERAL',
    title           VARCHAR(128) COMMENT '通知标题',
    content         TEXT COMMENT '通知内容',
    recipient_id    VARCHAR(64) COMMENT '收件人ID',
    recipient_name  VARCHAR(64) COMMENT '收件人姓名',
    encounter_id    BIGINT COMMENT '关联就诊ID',
    patient_id      BIGINT COMMENT '关联患者ID',
    priority        VARCHAR(16) DEFAULT 'MEDIUM' COMMENT '紧急程度: LOW/MEDIUM/HIGH/URGENT',
    channel         VARCHAR(32) COMMENT '发送渠道: STATION_LETTER/PAD_PUSH/SMS',
    is_read         TINYINT DEFAULT 0 COMMENT '已读标志',
    read_time       DATETIME COMMENT '已读时间',
    send_status     VARCHAR(16) DEFAULT 'PENDING' COMMENT '发送状态: PENDING/SENT/FAILED',

    is_deleted      TINYINT DEFAULT 0,
    created_by      VARCHAR(64) NOT NULL,
    created_time    DATETIME NOT NULL,
    updated_by      VARCHAR(64),
    updated_time    DATETIME,
    version         INT DEFAULT 0,
    trace_id        VARCHAR(64),

    INDEX idx_recipient (recipient_id),
    INDEX idx_type (notification_type),
    INDEX idx_status (send_status),
    INDEX idx_encounter (encounter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知记录';

-- -----------------------------------------------------------
-- 3. 审计日志表
-- 说明：全操作留痕，只追加不删除
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id         VARCHAR(64) COMMENT '操作人ID',
    user_name       VARCHAR(64) COMMENT '操作人姓名',
    action_type     VARCHAR(32) COMMENT '操作类型: CREATE/UPDATE/DELETE/VIEW',
    target_type     VARCHAR(32) COMMENT '操作对象类型: PATIENT/GREENWAY/ASSESSMENT',
    target_id       VARCHAR(64) COMMENT '操作对象ID',
    detail_json     JSON COMMENT '操作详情JSON',
    ip_address      VARCHAR(64) COMMENT '客户端IP',
    user_agent      VARCHAR(256) COMMENT '客户端信息',
    action_time     DATETIME COMMENT '操作时间',

    is_deleted      TINYINT DEFAULT 0,
    created_by      VARCHAR(64) NOT NULL,
    created_time    DATETIME NOT NULL,
    updated_by      VARCHAR(64),
    updated_time    DATETIME,
    version         INT DEFAULT 0,
    trace_id        VARCHAR(64),

    INDEX idx_user (user_id),
    INDEX idx_action_time (action_time),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志';
