SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

-- ---------------------------------------------------------------------
-- 金融押运公司 · 4 模块业务库
-- 库名 cash_escort；application.yml 里 ddl-auto: none，表结构全部由本文件在 MySQL 镜像构建期导入。
-- 注意：mysql 容器是 UTC，时间戳一律写 NOW()，日期用 DATE_SUB(CURDATE(), INTERVAL N DAY)。
-- 两处 schema（backend/src/main/resources/schema.sql 与 mysql/schema.sql）内容必须完全一致。
-- 外键顺序：被引用的父表（route / cash_vehicle / escort_member）先建，子表 escort_task 在后。

CREATE DATABASE IF NOT EXISTS cash_escort
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE cash_escort;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS escort_task;
DROP TABLE IF EXISTS escort_member;
DROP TABLE IF EXISTS cash_vehicle;
DROP TABLE IF EXISTS route;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------- 1. 押运线路 route（父表，先建） ----------------
CREATE TABLE route (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(64) COMMENT '线路名称',
    start_point  VARCHAR(64) COMMENT '起点',
    end_point    VARCHAR(64) COMMENT '终点',
    distance     INT COMMENT '里程（km）',
    risk_level   VARCHAR(8) COMMENT '风险等级：低 / 中 / 高',
    status       VARCHAR(8) COMMENT '状态：启用 / 停用',
    created_at   DATETIME COMMENT '创建时间',
    updated_at   DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 2. 运钞车 cash_vehicle（父表，先建） ----------------
CREATE TABLE cash_vehicle (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    plate         VARCHAR(16) COMMENT '车牌',
    model         VARCHAR(32) COMMENT '车型',
    capacity      DECIMAL(10,2) COMMENT '容量（万元）',
    status        VARCHAR(8) COMMENT '状态：可用 / 维修 / 停用',
    last_maintain DATE COMMENT '上次保养日期',
    created_at    DATETIME COMMENT '创建时间',
    updated_at    DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 3. 押运员 escort_member（父表，先建） ----------------
CREATE TABLE escort_member (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(32) COMMENT '姓名',
    cert_no    VARCHAR(32) COMMENT '持枪证号',
    level      VARCHAR(16) COMMENT '资质等级',
    status     VARCHAR(8) COMMENT '状态：在岗 / 休班 / 停用',
    join_date  DATE COMMENT '入队日期',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 4. 押运任务 escort_task（子表，最后建，引用上述三父表） ----------------
CREATE TABLE escort_task (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no    VARCHAR(32) COMMENT '任务编号',
    route_id   BIGINT COMMENT '线路 id（引用 route）',
    start_site VARCHAR(64) COMMENT '起点网点',
    end_site   VARCHAR(64) COMMENT '终点网点',
    amount     DECIMAL(14,2) COMMENT '押运金额（元）',
    member_id  BIGINT COMMENT '押运员 id（引用 escort_member）',
    vehicle_id BIGINT COMMENT '运钞车 id（引用 cash_vehicle）',
    status     VARCHAR(8) COMMENT '状态：待派车 / 执行中 / 已完成 / 异常',
    plan_date  DATE COMMENT '计划日期',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    CONSTRAINT fk_task_route FOREIGN KEY (route_id) REFERENCES route (id),
    CONSTRAINT fk_task_member FOREIGN KEY (member_id) REFERENCES escort_member (id),
    CONSTRAINT fk_task_vehicle FOREIGN KEY (vehicle_id) REFERENCES cash_vehicle (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ---------------------------------------------------------------------
-- 种子数据（含异常态，用于真实触发业务拒绝分支）
--   异常态 1：route4 状态「停用」→ 不可再派发新任务
--   异常态 2：vehicle3「维修」、vehicle4「停用」→ 不可被押运任务分配
--   异常态 3：member3「休班」、member4「停用」→ 不可被押运任务分配
--   异常态 4：task4 状态「异常」；task1 状态「待派车」尚未分配车与人
-- ---------------------------------------------------------------------

INSERT IGNORE INTO route (id, name, start_point, end_point, distance, risk_level, status, created_at, updated_at) VALUES
(1, '城东支行环线',   '城东支行', '中心金库', 12, '低', '启用', NOW(), NOW()),
(2, '机场押运专线',   '国际机场', '中心金库', 45, '中', '启用', NOW(), NOW()),
(3, '老城夜班线',     '老城支行', '西郊金库', 8,  '高', '启用', NOW(), NOW()),
(4, '高新区线',       '高新支行', '中心金库', 20, '中', '停用', NOW(), NOW());

INSERT IGNORE INTO cash_vehicle (id, plate, model, capacity, status, last_maintain, created_at, updated_at) VALUES
(1, '京A·0001', '依维柯运钞车', 500.00, '可用', DATE_SUB(CURDATE(), INTERVAL 30 DAY), NOW(), NOW()),
(2, '京A·0002', '全顺运钞车',   300.00, '可用', DATE_SUB(CURDATE(), INTERVAL 20 DAY), NOW(), NOW()),
(3, '京A·0003', '依维柯运钞车', 500.00, '维修', DATE_SUB(CURDATE(), INTERVAL 10 DAY), NOW(), NOW()),
(4, '京A·0004', '全顺运钞车',   300.00, '停用', DATE_SUB(CURDATE(), INTERVAL 60 DAY), NOW(), NOW());

INSERT IGNORE INTO escort_member (id, name, cert_no, level, status, join_date, created_at, updated_at) VALUES
(1, '张建国', 'GA11001', '一级', '在岗', DATE_SUB(CURDATE(), INTERVAL 90 DAY), NOW(), NOW()),
(2, '李卫国', 'GA11002', '一级', '在岗', DATE_SUB(CURDATE(), INTERVAL 80 DAY), NOW(), NOW()),
(3, '王铁柱', 'GA11003', '二级', '休班', DATE_SUB(CURDATE(), INTERVAL 70 DAY), NOW(), NOW()),
(4, '赵刚',   'GA11004', '二级', '停用', DATE_SUB(CURDATE(), INTERVAL 120 DAY), NOW(), NOW());

INSERT IGNORE INTO escort_task (id, task_no, route_id, start_site, end_site, amount, member_id, vehicle_id, status, plan_date, created_at, updated_at) VALUES
(1, 'T0001', 1, '城东支行', '中心金库', 1200.00, NULL,    NULL,    '待派车', DATE_SUB(CURDATE(), INTERVAL -1 DAY), NOW(), NOW()),
(2, 'T0002', 2, '国际机场', '中心金库', 3500.00, 1,       2,       '执行中', DATE_SUB(CURDATE(), INTERVAL -2 DAY), NOW(), NOW()),
(3, 'T0003', 2, '国际机场', '中心金库', 2800.00, 2,       2,       '已完成', DATE_SUB(CURDATE(), INTERVAL -10 DAY), NOW(), NOW()),
(4, 'T0004', 3, '老城支行', '西郊金库', 800.00,  1,       1,       '异常',   DATE_SUB(CURDATE(), INTERVAL -3 DAY), NOW(), NOW()),
(5, 'T0005', 1, '城东支行', '中心金库', 1500.00, 2,       1,       '执行中', DATE_SUB(CURDATE(), INTERVAL -1 DAY), NOW(), NOW()),
(6, 'T0006', 4, '高新支行', '中心金库', 2000.00, 1,       2,       '已完成', DATE_SUB(CURDATE(), INTERVAL -15 DAY), NOW(), NOW());
