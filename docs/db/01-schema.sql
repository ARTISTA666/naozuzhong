-- ============================================================
-- 脑卒中疾病管理医疗系统 — 数据库 Schema 定义
-- 版本: v1.0
-- 说明: 创建所有业务数据库
-- 设计文档: 4.2 节数据模型
-- ============================================================

-- 创建数据库（每个服务独立库，禁止跨服务直接访问）
CREATE DATABASE IF NOT EXISTS stroke_clinical
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS stroke_infra
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS stroke_integration
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS stroke_rehab
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- 通用审计字段说明（所有业务表强制包含）
-- 对应设计文档 4.1 节
-- ============================================================
-- is_deleted       TINYINT  DEFAULT 0         逻辑删除标志
-- created_by       VARCHAR(64) NOT NULL       创建人
-- created_time     DATETIME NOT NULL          创建时间
-- updated_by       VARCHAR(64)                更新人
-- updated_time     DATETIME                   更新时间
-- version          INT DEFAULT 0              乐观锁版本号
-- trace_id         VARCHAR(64)                请求链路ID
-- ============================================================

USE stroke_clinical;
