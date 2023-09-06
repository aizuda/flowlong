SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 添加数据库初始化脚本+兼容5.6/5.7版本MYSQL
-- ----------------------------
DROP DATABASE IF  EXISTS  `flowlong`;
CREATE DATABASE IF NOT EXISTS `flowlong` charset utf8mb4 collate utf8mb4_unicode_ci;
USE `flowlong`;

-- ----------------------------
-- Table structure for flw_his_instance
-- ----------------------------
DROP TABLE IF EXISTS `flw_his_instance`;
CREATE TABLE `flw_his_instance`  (
                                     `id` bigint NOT NULL COMMENT '主键ID',
                                     `tenant_id` varchar(50) COMMENT '租户ID',
                                     `create_id` varchar(50) NOT NULL COMMENT '创建人ID',
                                     `create_by` varchar(50) NOT NULL COMMENT '创建人',
                                     `create_time` timestamp NOT NULL COMMENT '创建时间',
                                     `process_id` bigint NOT NULL COMMENT '流程定义ID',
                                     `priority` tinyint(1) COMMENT '优先级',
                                     `instance_no` varchar(50) COMMENT '流程实例编号',
                                     `business_key` varchar(100) COMMENT '业务KEY',
                                     `variable` text COMMENT '变量json',
                                     `version` int COMMENT '版本',
                                     `expire_time` timestamp NULL COMMENT '期望完成时间',
                                     `last_update_by` varchar(50) COMMENT '上次更新人',
                                     `last_update_time` timestamp NULL COMMENT '上次更新时间',
                                     `instance_state` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态 0，结束 1，活动',
                                     `end_time` timestamp NULL COMMENT '结束时间',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     INDEX `idx_his_instance_process_id`(`process_id` ASC) USING BTREE,
                                     CONSTRAINT `fk_his_instance_process_id` FOREIGN KEY (`process_id`) REFERENCES `flw_process` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '流程实例表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for flw_his_task
-- ----------------------------
DROP TABLE IF EXISTS `flw_his_task`;
CREATE TABLE `flw_his_task`  (
                                 `id` bigint NOT NULL COMMENT '主键ID',
                                 `tenant_id` varchar(50) COMMENT '租户ID',
                                 `create_id` varchar(50) NOT NULL COMMENT '创建人ID',
                                 `create_by` varchar(50) NOT NULL COMMENT '创建人',
                                 `create_time` timestamp NOT NULL COMMENT '创建时间',
                                 `instance_id` bigint NOT NULL COMMENT '流程实例ID',
                                 `parent_task_id` bigint COMMENT '父任务ID',
                                 `task_name` varchar(100)   NOT NULL COMMENT '任务名称',
                                 `display_name` varchar(200)   NOT NULL COMMENT '任务显示名称',
                                 `task_type` tinyint(1) NOT NULL COMMENT '任务类型',
                                 `perform_type` tinyint(1) COMMENT '参与类型',
                                 `action_url` varchar(200) COMMENT '任务处理的url',
                                 `variable` text COMMENT '变量json',
                                 `assignor_id` varchar(100) COMMENT '委托人ID',
                                 `assignor` varchar(100) COMMENT '委托人',
                                 `expire_time` timestamp NULL COMMENT '任务期望完成时间',
                                 `remind_time` timestamp NULL COMMENT '提醒时间',
                                 `remind_repeat` tinyint(1) NOT NULL DEFAULT 0 COMMENT '提醒次数',
                                 `read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '已阅 0，否 1，是',
                                 `finish_time` timestamp NULL COMMENT '任务完成时间',
                                 `task_state` tinyint(1) NOT NULL DEFAULT 1 COMMENT '任务状态 0，活动 1，结束 2，超时 3，终止',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 INDEX `idx_his_task_instance_id`(`instance_id` ASC) USING BTREE,
                                 INDEX `idx_his_task_parent_task_id`(`parent_task_id` ASC) USING BTREE,
                                 CONSTRAINT `fk_his_task_instance_id` FOREIGN KEY (`instance_id`) REFERENCES `flw_his_instance` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '任务表' ROW_FORMAT = Dynamic;

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
                                       `type` int NOT NULL COMMENT '类型 0，用户 1，角色 2，部门',
                                       PRIMARY KEY (`id`) USING BTREE,
                                       INDEX `idx_his_task_actor_task_id`(`task_id` ASC) USING BTREE,
                                       CONSTRAINT `fk_his_task_actor_task_id` FOREIGN KEY (`task_id`) REFERENCES `flw_his_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '历史任务参与者表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for flw_instance
-- ----------------------------
DROP TABLE IF EXISTS `flw_instance`;
CREATE TABLE `flw_instance`  (
                                 `id` bigint NOT NULL COMMENT '主键ID',
                                 `tenant_id` varchar(50) COMMENT '租户ID',
                                 `create_id` varchar(50) NOT NULL COMMENT '创建人ID',
                                 `create_by` varchar(50) NOT NULL COMMENT '创建人',
                                 `create_time` timestamp NOT NULL COMMENT '创建时间',
                                 `process_id` bigint NOT NULL COMMENT '流程定义ID',
                                 `priority` tinyint(1) COMMENT '优先级',
                                 `instance_no` varchar(50) COMMENT '流程实例编号',
                                 `business_key` varchar(100) COMMENT '业务KEY',
                                 `variable` text COMMENT '变量json',
                                 `version` int COMMENT '版本',
                                 `expire_time` timestamp NULL COMMENT '期望完成时间',
                                 `last_update_by` varchar(50) COMMENT '上次更新人',
                                 `last_update_time` timestamp NULL COMMENT '上次更新时间',
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
                                `create_by` varchar(50) NOT NULL COMMENT '创建人',
                                `create_time` timestamp NOT NULL COMMENT '创建时间',
                                `name` varchar(100) NOT NULL COMMENT '流程名称',
                                `display_name` varchar(200) COMMENT '流程显示名称',
                                `icon` varchar(255) DEFAULT NULL COMMENT '图标地址',
                                `type` varchar(100) COMMENT '流程类型',
                                `version` int NOT NULL DEFAULT 1 COMMENT '版本，默认 1',
                                `instance_url` varchar(200) COMMENT '实例地址',
                                `state` tinyint(1) DEFAULT 1 COMMENT '流程是否可用 0，否 1，是',
                                `content` text COMMENT '流程模型定义',
                                PRIMARY KEY (`id`) USING BTREE,
                                INDEX `idx_process_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '流程定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for flw_task
-- ----------------------------
DROP TABLE IF EXISTS `flw_task`;
CREATE TABLE `flw_task`  (
                             `id` bigint NOT NULL COMMENT '主键ID',
                             `tenant_id` varchar(50) COMMENT '租户ID',
                             `create_id` varchar(50) NOT NULL COMMENT '创建人ID',
                             `create_by` varchar(50) NOT NULL COMMENT '创建人',
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
                             `expire_time` timestamp NULL COMMENT '任务期望完成时间',
                             `remind_time` timestamp NULL COMMENT '提醒时间',
                             `remind_repeat` tinyint(1) NOT NULL DEFAULT 0 COMMENT '提醒次数',
                             `read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '已阅 0，否 1，是',
                             `finish_time` timestamp NULL COMMENT '完成时间',
                             PRIMARY KEY (`id`) USING BTREE,
                             INDEX `idx_task_instance_id`(`instance_id` ASC) USING BTREE,
                             CONSTRAINT `fk_task_instance_id` FOREIGN KEY (`instance_id`) REFERENCES `flw_instance` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '任务表' ROW_FORMAT = Dynamic;

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
                                   `type` int NOT NULL COMMENT '类型 0，用户 1，角色 2，部门',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `idx_task_actor_task_id`(`task_id` ASC) USING BTREE,
                                   CONSTRAINT `fk_task_actor_task_id` FOREIGN KEY (`task_id`) REFERENCES `flw_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '任务参与者表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for flw_task_cc
-- ----------------------------
DROP TABLE IF EXISTS `flw_task_cc`;
CREATE TABLE `flw_task_cc`  (
                                `id` bigint NOT NULL COMMENT '主键ID',
                                `tenant_id` varchar(50) COMMENT '租户ID',
                                `create_id` varchar(50) NOT NULL COMMENT '创建人ID',
                                `create_by` varchar(50) NOT NULL COMMENT '创建人',
                                `create_time` timestamp NOT NULL COMMENT '创建时间',
                                `instance_id` bigint NOT NULL COMMENT '流程实例ID',
                                `parent_task_id` bigint COMMENT '父任务ID',
                                `task_name` varchar(100) NOT NULL COMMENT '任务名称',
                                `display_name` varchar(200) NOT NULL COMMENT '任务显示名称',
                                `actor_id` varchar(300) NOT NULL COMMENT '参与者ID',
                                `actor_name` varchar(300) NOT NULL COMMENT '参与者名称',
                                `type` int NOT NULL COMMENT '类型 0，用户 1，角色 2，部门',
                                `state` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态 0，结束 1，活动',
                                `finish_time` timestamp NULL COMMENT '完成时间',
                                PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '抄送任务表' ROW_FORMAT = Dynamic;
