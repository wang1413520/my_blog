-- ============================================
-- 文件夹上传功能 - 数据库迁移脚本
-- 目标数据库: my_web_school_project
-- 创建日期: 2026-06-26
-- ============================================

-- 1. 扩展 resource 表，新增文件夹上传相关字段
ALTER TABLE resource
    ADD COLUMN upload_type VARCHAR(20) NOT NULL DEFAULT 'single' COMMENT '上传类型：single(单文件)/folder(文件夹)',
    ADD COLUMN batch_no VARCHAR(32) DEFAULT NULL COMMENT '文件夹上传批次号，如 RB202606260001',
    ADD COLUMN folder_name VARCHAR(255) DEFAULT NULL COMMENT '所属根文件夹名称（仅 folder 上传时有值）',
    ADD COLUMN relative_path VARCHAR(500) DEFAULT NULL COMMENT '文件在文件夹内的相对路径（仅 folder 上传时有值）';

-- 为 batch_no 建索引，方便按批次查询
CREATE INDEX idx_resource_batch_no ON resource(batch_no);

-- 2. 创建文件夹上传批次表
CREATE TABLE IF NOT EXISTS resource_upload_batch (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    batch_no        VARCHAR(32) NOT NULL COMMENT '上传批次号，格式 RB+yyyyMMdd+4位序号',
    user_id         BIGINT NOT NULL COMMENT '上传用户ID',
    folder_name     VARCHAR(255) NOT NULL COMMENT '根文件夹名称',
    description     VARCHAR(500) DEFAULT NULL COMMENT '批次描述',
    total_count     INT NOT NULL DEFAULT 0 COMMENT '文件总数',
    success_count   INT NOT NULL DEFAULT 0 COMMENT '成功上传数',
    fail_count      INT NOT NULL DEFAULT 0 COMMENT '失败数',
    total_size      BIGINT NOT NULL DEFAULT 0 COMMENT '总大小（字节）',
    status          TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-处理中 1-全部成功 2-部分成功 3-全部失败',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_batch_no (batch_no),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资料文件夹上传批次表';
