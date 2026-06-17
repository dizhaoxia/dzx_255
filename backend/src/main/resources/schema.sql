-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    role VARCHAR(32) NOT NULL,
    email VARCHAR(128),
    phone VARCHAR(32),
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 岗位表
CREATE TABLE IF NOT EXISTS position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    position_name VARCHAR(128) NOT NULL,
    position_desc TEXT,
    department VARCHAR(64),
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 候选人表
CREATE TABLE IF NOT EXISTS candidate (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    gender VARCHAR(8),
    age INT,
    phone VARCHAR(32),
    email VARCHAR(128),
    position_id BIGINT NOT NULL,
    education VARCHAR(64),
    work_years INT,
    resume_url VARCHAR(256),
    status VARCHAR(32) DEFAULT 'RESUME_SCREEN',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 评分维度表
CREATE TABLE IF NOT EXISTS evaluation_dimension (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dimension_code VARCHAR(64) NOT NULL UNIQUE,
    dimension_name VARCHAR(64) NOT NULL,
    dimension_desc VARCHAR(256),
    max_score INT DEFAULT 5,
    min_score INT DEFAULT 1,
    sort_order INT DEFAULT 0,
    is_default TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 面试任务表
CREATE TABLE IF NOT EXISTS interview_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    position_id BIGINT NOT NULL,
    interviewer_id BIGINT NOT NULL,
    interview_round INT DEFAULT 1,
    interview_type VARCHAR(32),
    interview_time TIMESTAMP,
    video_url VARCHAR(512),
    status VARCHAR(32) DEFAULT 'PENDING',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 评价记录表
CREATE TABLE IF NOT EXISTS evaluation_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    candidate_id BIGINT NOT NULL,
    interviewer_id BIGINT NOT NULL,
    dimension_id BIGINT NOT NULL,
    dimension_code VARCHAR(64) NOT NULL,
    score INT NOT NULL,
    comment TEXT,
    selected_tags TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 评价汇总表
CREATE TABLE IF NOT EXISTS evaluation_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL UNIQUE,
    candidate_id BIGINT NOT NULL,
    interviewer_id BIGINT NOT NULL,
    overall_comment TEXT,
    hire_suggestion VARCHAR(32),
    total_score DECIMAL(5,2),
    weighted_score DECIMAL(5,2),
    status VARCHAR(32) DEFAULT 'DRAFT',
    submit_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 行为事例标签表
CREATE TABLE IF NOT EXISTS behavior_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dimension_id BIGINT,
    tag_name VARCHAR(64) NOT NULL,
    tag_type VARCHAR(16) DEFAULT 'NEUTRAL',
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 评价模板表
CREATE TABLE IF NOT EXISTS evaluation_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_name VARCHAR(128) NOT NULL,
    template_desc VARCHAR(512),
    is_default TINYINT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 模板维度关联表（含权重）
CREATE TABLE IF NOT EXISTS template_dimension (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    dimension_id BIGINT NOT NULL,
    dimension_code VARCHAR(64) NOT NULL,
    dimension_name VARCHAR(64) NOT NULL,
    dimension_desc VARCHAR(256),
    weight_percent INT DEFAULT 0,
    max_score INT DEFAULT 5,
    min_score INT DEFAULT 1,
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 岗位模板绑定表
CREATE TABLE IF NOT EXISTS position_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    position_id BIGINT NOT NULL UNIQUE,
    template_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);
