-- ============================================
-- 新表创建脚本 — 用于帖子详情/评论/资料模块
-- 数据库：my_web_school_project
-- ============================================

-- 1. 帖子点赞表（记录每个用户对每个帖子的点赞关系）
CREATE TABLE IF NOT EXISTS post_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_post_user (post_id, user_id),
    KEY idx_post_id (post_id),
    KEY idx_user_id (user_id)
) COMMENT '帖子点赞记录表';

-- 2. 评论表
CREATE TABLE IF NOT EXISTS comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL COMMENT '关联帖子ID',
    user_id BIGINT NOT NULL COMMENT '评论者用户ID',
    content VARCHAR(500) NOT NULL COMMENT '评论内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_post_id (post_id),
    KEY idx_user_id (user_id)
) COMMENT '评论表';

-- 3. 学习资料表
CREATE TABLE IF NOT EXISTS resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '上传者ID',
    title VARCHAR(255) NOT NULL COMMENT '资料名称',
    description TEXT COMMENT '资料描述',
    file_url VARCHAR(500) NOT NULL COMMENT 'OSS文件路径',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型（pdf/doc/docx）',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    KEY idx_user_id (user_id),
    KEY idx_file_type (file_type)
) COMMENT '学习资料表';

-- 4. 文件转换记录表（工具箱）
CREATE TABLE IF NOT EXISTS convert_record (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       DEFAULT NULL           COMMENT '用户ID（可为空，未登录用户）',
    source_name VARCHAR(255) NOT NULL                COMMENT '源文件名',
    source_type VARCHAR(20)  NOT NULL                COMMENT '源文件格式（pdf/docx/png...）',
    target_type VARCHAR(20)  NOT NULL                COMMENT '目标文件格式',
    status      TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0-转换中 1-成功 2-失败',
    source_url  VARCHAR(500) NOT NULL                COMMENT '源文件 OSS URL',
    result_url  VARCHAR(500) DEFAULT NULL            COMMENT '结果文件 OSS URL',
    file_size   BIGINT       DEFAULT 0               COMMENT '源文件大小（字节）',
    error_msg   VARCHAR(500) DEFAULT NULL            COMMENT '失败原因',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) COMMENT '文件转换记录表';
