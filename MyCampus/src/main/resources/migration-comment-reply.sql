-- ============================================
-- 评论楼中楼（多级回复）功能 - 数据库迁移脚本
-- 目标数据库: my_web_school_project
-- 创建日期: 2026-08-10
-- 说明: 不改原 comment 表结构，新增 comment_reply 表承载所有"回复"。
--       root_comment_id 指向 comment 表顶层评论 id；parent_id 指向 comment_reply 表记录 id（NULL=直接回复顶层）。
-- ============================================

CREATE TABLE IF NOT EXISTS comment_reply (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    post_id           BIGINT       NOT NULL COMMENT '所属帖子ID（冗余，便于直接按帖子查+防串楼校验）',
    root_comment_id   BIGINT       NOT NULL COMMENT '挂在哪条顶层评论下（comment 表的id），顶层评论之间不串楼',
    parent_id         BIGINT       DEFAULT NULL COMMENT '直接回复的评论id：NULL=直接回复顶层评论；否则=另一条回复的id（comment_reply.id）',
    user_id           BIGINT       NOT NULL COMMENT '回复人ID',
    reply_to_user_id  BIGINT       DEFAULT NULL COMMENT '被@的用户ID：直接回复顶层评论且未传时为顶层评论作者ID；回复某人时=被回复人ID',
    content           VARCHAR(500) NOT NULL COMMENT '回复内容，不超过500字',
    status            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-正常 0-已删除（软删）',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_post_root (post_id, root_comment_id),
    KEY idx_root (root_comment_id),
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论回复（楼中楼）表';
