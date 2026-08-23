-- ============================================
-- 公告板功能 - 数据库迁移脚本
-- 目标数据库: my_web_school_project
-- 创建日期: 2026-08-10
-- 说明: update_time 是"版本号"核心字段，前端用它判断公告是否有更新。
--       数据库 ON UPDATE CURRENT_TIMESTAMP 只在行数据实际变化时触发，
--       后端 UPDATE 时必须显式 update_time=NOW() 保证每次编辑都产生新版本。
-- ============================================

CREATE TABLE IF NOT EXISTS notice (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title`       VARCHAR(100) NOT NULL COMMENT '公告标题',
  `content`     TEXT         NOT NULL COMMENT '公告内容（支持换行，按原文展示）',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（作为版本号，编辑时强制刷新）',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告板';
