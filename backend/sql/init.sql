-- 创建数据库
CREATE DATABASE IF NOT EXISTS interview_evaluation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE interview_evaluation;

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码',
    real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
    role VARCHAR(32) NOT NULL COMMENT '角色：HR-人力资源，INTERVIEWER-面试官',
    email VARCHAR(128) COMMENT '邮箱',
    phone VARCHAR(32) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 岗位表
DROP TABLE IF EXISTS position;
CREATE TABLE position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '岗位ID',
    position_name VARCHAR(128) NOT NULL COMMENT '岗位名称',
    position_desc TEXT COMMENT '岗位描述',
    department VARCHAR(64) COMMENT '所属部门',
    status TINYINT DEFAULT 1 COMMENT '状态：1-招聘中，0-已关闭',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- 候选人表
DROP TABLE IF EXISTS candidate;
CREATE TABLE candidate (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '候选人ID',
    name VARCHAR(64) NOT NULL COMMENT '姓名',
    gender VARCHAR(8) COMMENT '性别',
    age INT COMMENT '年龄',
    phone VARCHAR(32) COMMENT '手机号',
    email VARCHAR(128) COMMENT '邮箱',
    position_id BIGINT NOT NULL COMMENT '应聘岗位ID',
    education VARCHAR(64) COMMENT '学历',
    work_years INT COMMENT '工作年限',
    resume_url VARCHAR(256) COMMENT '简历地址',
    status VARCHAR(32) DEFAULT 'PENDING' COMMENT '状态：PENDING-待面试，INTERVIEWING-面试中，COMPLETED-面试完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候选人表';

-- 评分维度表
DROP TABLE IF EXISTS evaluation_dimension;
CREATE TABLE evaluation_dimension (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '维度ID',
    dimension_code VARCHAR(64) NOT NULL UNIQUE COMMENT '维度编码',
    dimension_name VARCHAR(64) NOT NULL COMMENT '维度名称',
    dimension_desc VARCHAR(256) COMMENT '维度描述',
    max_score INT DEFAULT 5 COMMENT '最高分',
    min_score INT DEFAULT 1 COMMENT '最低分',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_default TINYINT DEFAULT 1 COMMENT '是否默认维度：1-是，0-否',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分维度表';

-- 面试任务表
DROP TABLE IF EXISTS interview_task;
CREATE TABLE interview_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    candidate_id BIGINT NOT NULL COMMENT '候选人ID',
    position_id BIGINT NOT NULL COMMENT '岗位ID',
    interviewer_id BIGINT NOT NULL COMMENT '面试官ID',
    interview_round INT DEFAULT 1 COMMENT '面试轮次',
    interview_type VARCHAR(32) COMMENT '面试类型：技术面，综合面，HR面',
    interview_time DATETIME COMMENT '面试时间',
    status VARCHAR(32) DEFAULT 'PENDING' COMMENT '状态：PENDING-待评价，DRAFT-草稿中，SUBMITTED-已评价',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_interviewer (interviewer_id),
    INDEX idx_candidate (candidate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试任务表';

-- 评价记录表
DROP TABLE IF EXISTS evaluation_record;
CREATE TABLE evaluation_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID',
    task_id BIGINT NOT NULL COMMENT '面试任务ID',
    candidate_id BIGINT NOT NULL COMMENT '候选人ID',
    interviewer_id BIGINT NOT NULL COMMENT '面试官ID',
    dimension_id BIGINT NOT NULL COMMENT '评分维度ID',
    dimension_code VARCHAR(64) NOT NULL COMMENT '维度编码',
    score INT NOT NULL COMMENT '评分',
    comment TEXT COMMENT '该维度评语',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_task (task_id),
    INDEX idx_candidate (candidate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价记录表';

-- 评价汇总表（综合评价和录用建议）
DROP TABLE IF EXISTS evaluation_summary;
CREATE TABLE evaluation_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '汇总ID',
    task_id BIGINT NOT NULL UNIQUE COMMENT '面试任务ID',
    candidate_id BIGINT NOT NULL COMMENT '候选人ID',
    interviewer_id BIGINT NOT NULL COMMENT '面试官ID',
    overall_comment TEXT COMMENT '综合评价',
    hire_suggestion VARCHAR(32) COMMENT '录用建议：STRONG_RECOMMEND-强烈推荐，RECOMMEND-推荐，PENDING-待定，NOT_RECOMMEND-不推荐',
    total_score DECIMAL(5,2) COMMENT '总平均分',
    status VARCHAR(32) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，SUBMITTED-已提交',
    submit_time DATETIME COMMENT '提交时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价汇总表';

-- 插入初始数据
-- 插入评分维度
INSERT INTO evaluation_dimension (dimension_code, dimension_name, dimension_desc, max_score, min_score, sort_order, is_default) VALUES
('PROFESSIONAL_SKILL', '专业技能', '考察候选人的专业知识和技术能力', 5, 1, 1, 1),
('COMMUNICATION', '沟通表达', '考察候选人的语言表达和沟通能力', 5, 1, 2, 1),
('TEAMWORK', '团队协作', '考察候选人的团队合作精神和协作能力', 5, 1, 3, 1),
('JOB_FIT', '岗位匹配度', '考察候选人与岗位要求的匹配程度', 5, 1, 4, 1),
('LEARNING_ABILITY', '学习能力', '考察候选人的学习能力和发展潜力', 5, 1, 5, 1);

-- 插入初始用户（密码为 123456 的 BCrypt 加密值）
INSERT INTO sys_user (username, password, real_name, role, email, phone, status) VALUES
('hr001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张HR', 'HR', 'hr001@example.com', '13800000001', 1),
('interviewer001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李技术', 'INTERVIEWER', 'lijs@example.com', '13800000002', 1),
('interviewer002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '王技术', 'INTERVIEWER', 'wangjs@example.com', '13800000003', 1);

-- 插入初始岗位
INSERT INTO position (position_name, position_desc, department, status) VALUES
('高级Java开发工程师', '负责公司核心业务系统的设计与开发', '技术部', 1),
('前端开发工程师', '负责公司Web端产品的前端开发', '技术部', 1),
('产品经理', '负责产品规划和需求分析', '产品部', 1);

-- 插入初始候选人
INSERT INTO candidate (name, gender, age, phone, email, position_id, education, work_years, status) VALUES
('张三', '男', 28, '13900000001', 'zhangsan@example.com', 1, '本科', 5, 'INTERVIEWING'),
('李四', '女', 25, '13900000002', 'lisi@example.com', 1, '硕士', 3, 'INTERVIEWING'),
('王五', '男', 30, '13900000003', 'wangwu@example.com', 2, '本科', 7, 'PENDING');

-- 插入面试任务
INSERT INTO interview_task (candidate_id, position_id, interviewer_id, interview_round, interview_type, interview_time, status) VALUES
(1, 1, 2, 1, '技术面', '2024-01-15 10:00:00', 'PENDING'),
(1, 1, 3, 2, '技术面', '2024-01-16 14:00:00', 'PENDING'),
(2, 1, 2, 1, '技术面', '2024-01-17 09:30:00', 'PENDING'),
(3, 2, 2, 1, '技术面', '2024-01-18 11:00:00', 'PENDING');
