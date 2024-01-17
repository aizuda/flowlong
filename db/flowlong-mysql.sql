SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 添加数据库初始化脚本+兼容5.6/5.7版本MYSQL
-- ----------------------------
DROP DATABASE IF  EXISTS  `flowlong`;
CREATE DATABASE IF NOT EXISTS `flowlong` charset utf8mb4 collate utf8mb4_unicode_ci;
USE `flowlong`;

-- ----------------------------
-- Table structure for flw_his_task_actor
-- ----------------------------
DROP TABLE IF EXISTS `flw_his_task_actor`;
CREATE TABLE `flw_his_task_actor`  (
    `id` bigint NOT NULL COMMENT '主键 ID',
    `tenant_id` varchar(50) COMMENT '租户ID',
    `instance_id` bigint NOT NULL COMMENT '流程实例ID',
    `task_id` bigint NOT NULL COMMENT '任务ID',
    `actor_id` varchar(100) NOT NULL COMMENT '参与者ID',
    `actor_name` varchar(100) NOT NULL COMMENT '参与者名称',
    `actor_type` int NOT NULL COMMENT '参与者类型 0，用户 1，角色 2，部门',
    `weight` int COMMENT '票签权重',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_his_task_actor_task_id`(`task_id` ASC) USING BTREE,
    CONSTRAINT `fk_his_task_actor_task_id` FOREIGN KEY (`task_id`) REFERENCES `flw_his_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '历史任务参与者表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for flw_his_task
-- ----------------------------
DROP TABLE IF EXISTS `flw_his_task`;
CREATE TABLE `flw_his_task`  (
    `id` bigint NOT NULL COMMENT '主键ID',
    `tenant_id` varchar(50) COMMENT '租户ID',
    `create_id` varchar(50) NOT NULL COMMENT '创建人ID',
    `create_by` varchar(50) NOT NULL COMMENT '创建人名称',
    `create_time` timestamp NOT NULL COMMENT '创建时间',
    `instance_id` bigint NOT NULL COMMENT '流程实例ID',
    `parent_task_id` bigint COMMENT '父任务ID',
    `call_process_id` bigint COMMENT '调用外部流程定义ID',
    `call_instance_id` bigint COMMENT '调用外部流程实例ID',
    `task_name` varchar(100)   NOT NULL COMMENT '任务名称',
    `display_name` varchar(200)   NOT NULL COMMENT '任务显示名称',
    `task_type` tinyint(1) NOT NULL COMMENT '任务类型',
    `perform_type` tinyint(1) COMMENT '参与类型',
    `action_url` varchar(200) COMMENT '任务处理的url',
    `variable` text COMMENT '变量json',
    `assignor_id` varchar(100) COMMENT '委托人ID',
    `assignor` varchar(100) COMMENT '委托人',
    `expire_time` timestamp NULL DEFAULT NULL COMMENT '任务期望完成时间',
    `remind_time` timestamp NULL DEFAULT NULL COMMENT '提醒时间',
    `remind_repeat` tinyint(1) NOT NULL DEFAULT 0 COMMENT '提醒次数',
    `viewed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '已阅 0，否 1，是',
    `finish_time` timestamp NULL DEFAULT NULL COMMENT '任务完成时间',
    `task_state` tinyint(1) NOT NULL DEFAULT 1 COMMENT '任务状态 0，活动 1，结束 2，拒绝 3，超时 4，终止  5，跳转',
    `duration` bigint COMMENT '处理耗时',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_his_task_instance_id`(`instance_id` ASC) USING BTREE,
    INDEX `idx_his_task_parent_task_id`(`parent_task_id` ASC) USING BTREE,
    CONSTRAINT `fk_his_task_instance_id` FOREIGN KEY (`instance_id`) REFERENCES `flw_his_instance` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '历史任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for flw_task_actor
-- ----------------------------
DROP TABLE IF EXISTS `flw_task_actor`;
CREATE TABLE `flw_task_actor`  (
    `id` bigint NOT NULL COMMENT '主键 ID',
    `tenant_id` varchar(50) COMMENT '租户ID',
    `instance_id` bigint NOT NULL COMMENT '流程实例ID',
    `task_id` bigint NOT NULL COMMENT '任务ID',
    `actor_id` varchar(100) NOT NULL COMMENT '参与者ID',
    `actor_name` varchar(100) NOT NULL COMMENT '参与者名称',
    `actor_type` int NOT NULL COMMENT '参与者类型 0，用户 1，角色 2，部门',
    `weight` int COMMENT '票签权重',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_task_actor_task_id`(`task_id` ASC) USING BTREE,
    CONSTRAINT `fk_task_actor_task_id` FOREIGN KEY (`task_id`) REFERENCES `flw_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '任务参与者表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for flw_task
-- ----------------------------
DROP TABLE IF EXISTS `flw_task`;
CREATE TABLE `flw_task`  (
    `id` bigint NOT NULL COMMENT '主键ID',
    `tenant_id` varchar(50) COMMENT '租户ID',
    `create_id` varchar(50) NOT NULL COMMENT '创建人ID',
    `create_by` varchar(50) NOT NULL COMMENT '创建人名称',
    `create_time` timestamp NOT NULL COMMENT '创建时间',
    `instance_id` bigint NOT NULL COMMENT '流程实例ID',
    `parent_task_id` bigint COMMENT '父任务ID',
    `task_name` varchar(100) NOT NULL COMMENT '任务名称',
    `display_name` varchar(200) NOT NULL COMMENT '任务显示名称',
    `task_type` tinyint(1) NOT NULL COMMENT '任务类型',
    `perform_type` tinyint(1) NULL COMMENT '参与类型',
    `action_url` varchar(200) COMMENT '任务处理的url',
    `variable` text COMMENT '变量json',
    `assignor_id` varchar(100) COMMENT '委托人ID',
    `assignor` varchar(100) COMMENT '委托人',
    `expire_time` timestamp NULL DEFAULT NULL COMMENT '任务期望完成时间',
    `remind_time` timestamp NULL DEFAULT NULL COMMENT '提醒时间',
    `remind_repeat` tinyint(1) NOT NULL DEFAULT 0 COMMENT '提醒次数',
    `viewed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '已阅 0，否 1，是',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_task_instance_id`(`instance_id` ASC) USING BTREE,
    CONSTRAINT `fk_task_instance_id` FOREIGN KEY (`instance_id`) REFERENCES `flw_instance` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for flw_his_instance
-- ----------------------------
DROP TABLE IF EXISTS `flw_his_instance`;
CREATE TABLE `flw_his_instance`  (
    `id` bigint NOT NULL COMMENT '主键ID',
    `tenant_id` varchar(50) COMMENT '租户ID',
    `create_id` varchar(50) NOT NULL COMMENT '创建人ID',
    `create_by` varchar(50) NOT NULL COMMENT '创建人名称',
    `create_time` timestamp NOT NULL COMMENT '创建时间',
    `process_id` bigint NOT NULL COMMENT '流程定义ID',
    `parent_instance_id` bigint COMMENT '父流程实例ID',
    `priority` tinyint(1) COMMENT '优先级',
    `instance_no` varchar(50) COMMENT '流程实例编号',
    `business_key` varchar(100) COMMENT '业务KEY',
    `variable` text COMMENT '变量json',
    `current_node` varchar(100) NOT NULL COMMENT '当前所在节点',
    `expire_time` timestamp NULL DEFAULT NULL COMMENT '期望完成时间',
    `last_update_by` varchar(50) COMMENT '上次更新人',
    `last_update_time` timestamp NULL DEFAULT NULL COMMENT '上次更新时间',
    `instance_state` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态 0，审批中 1，审批通过 2，审批拒绝 3，撤销审批 4，超时结束 5，强制终止',
    `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
    `duration` bigint COMMENT '处理耗时',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_his_instance_process_id`(`process_id` ASC) USING BTREE,
    CONSTRAINT `fk_his_instance_process_id` FOREIGN KEY (`process_id`) REFERENCES `flw_process` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '历史流程实例表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for flw_instance
-- ----------------------------
DROP TABLE IF EXISTS `flw_instance`;
CREATE TABLE `flw_instance`  (
    `id` bigint NOT NULL COMMENT '主键ID',
    `tenant_id` varchar(50) COMMENT '租户ID',
    `create_id` varchar(50) NOT NULL COMMENT '创建人ID',
    `create_by` varchar(50) NOT NULL COMMENT '创建人名称',
    `create_time` timestamp NOT NULL COMMENT '创建时间',
    `process_id` bigint NOT NULL COMMENT '流程定义ID',
    `parent_instance_id` bigint COMMENT '父流程实例ID',
    `priority` tinyint(1) COMMENT '优先级',
    `instance_no` varchar(50) COMMENT '流程实例编号',
    `business_key` varchar(100) COMMENT '业务KEY',
    `variable` text COMMENT '变量json',
    `current_node` varchar(100) NOT NULL COMMENT '当前所在节点',
    `expire_time` timestamp NULL DEFAULT NULL COMMENT '期望完成时间',
    `last_update_by` varchar(50) COMMENT '上次更新人',
    `last_update_time` timestamp NULL DEFAULT NULL COMMENT '上次更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_instance_process_id`(`process_id` ASC) USING BTREE,
    CONSTRAINT `fk_instance_process_id` FOREIGN KEY (`process_id`) REFERENCES `flw_process` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '流程实例表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for flw_process
-- ----------------------------
DROP TABLE IF EXISTS `flw_process`;
CREATE TABLE `flw_process`  (
    `id` bigint NOT NULL COMMENT '主键ID',
    `tenant_id` varchar(50) COMMENT '租户ID',
    `create_id` varchar(50) NOT NULL COMMENT '创建人ID',
    `create_by` varchar(50) NOT NULL COMMENT '创建人名称',
    `create_time` timestamp NOT NULL COMMENT '创建时间',
    `process_key` varchar(100) NOT NULL COMMENT '流程定义 key 唯一标识',
    `process_name` varchar(100) NOT NULL COMMENT '流程定义名称',
    `process_icon` varchar(255) DEFAULT NULL COMMENT '流程图标地址',
    `process_type` varchar(100) COMMENT '流程类型',
    `process_version` int NOT NULL DEFAULT 1 COMMENT '流程版本，默认 1',
    `instance_url` varchar(200) COMMENT '实例地址',
    `remark` varchar(255) COMMENT '备注说明',
    `use_scope` tinyint(1) NOT NULL DEFAULT 0 COMMENT '使用范围 0，全员 1，指定人员（业务关联） 2，均不可提交',
    `process_state` tinyint(1) NOT NULL DEFAULT 1 COMMENT '流程状态 0，不可用 1，可用',
    `model_content` text COMMENT '流程模型定义JSON内容',
    `sort` tinyint(1) DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_process_name`(`process_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '流程定义表' ROW_FORMAT = Dynamic;
