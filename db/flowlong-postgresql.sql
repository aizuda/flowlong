
-- ----------------------------
-- Table structure for flw_process
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_process";
CREATE TABLE "public"."flw_process" (
                                        "id" int8 NOT NULL,
                                        "tenant_id" varchar(50) COLLATE "pg_catalog"."default",
                                        "create_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
                                        "create_by" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
                                        "create_time" timestamp(6) NOT NULL,
                                        "process_key" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
                                        "process_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
                                        "process_icon" varchar(255) COLLATE "pg_catalog"."default",
                                        "process_type" varchar(100) COLLATE "pg_catalog"."default",
                                        "process_version" int4 NOT NULL,
                                        "instance_url" varchar(200) COLLATE "pg_catalog"."default",
                                        "use_scope" int2 NOT NULL DEFAULT 0,
                                        "process_state" int2 NOT NULL DEFAULT 1,
                                        "model_content" text COLLATE "pg_catalog"."default",
                                        "sort" int2
)
;
COMMENT ON COLUMN "public"."flw_process"."id" IS '主键ID';
COMMENT ON COLUMN "public"."flw_process"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "public"."flw_process"."create_id" IS '创建人ID';
COMMENT ON COLUMN "public"."flw_process"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."flw_process"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."flw_process"."process_key" IS '流程定义 key 唯一标识';
COMMENT ON COLUMN "public"."flw_process"."process_name" IS '流程定义名称';
COMMENT ON COLUMN "public"."flw_process"."process_icon" IS '流程图标地址';
COMMENT ON COLUMN "public"."flw_process"."process_type" IS '流程类型';
COMMENT ON COLUMN "public"."flw_process"."process_version" IS '流程版本，默认 1';
COMMENT ON COLUMN "public"."flw_process"."instance_url" IS '实例地址';
COMMENT ON COLUMN "public"."flw_process"."use_scope" IS '使用范围 0，全员 1，指定人员（业务关联） 2，均不可提交';
COMMENT ON COLUMN "public"."flw_process"."process_state" IS '流程状态 0，不可用 1，可用';
COMMENT ON COLUMN "public"."flw_process"."model_content" IS '流程模型定义JSON内容';
COMMENT ON COLUMN "public"."flw_process"."sort" IS '排序';
COMMENT ON TABLE "public"."flw_process" IS '流程定义表';

-- ----------------------------
-- Table structure for flw_his_instance
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_his_instance";
CREATE TABLE "public"."flw_his_instance" (
                                             "id" int8 NOT NULL,
                                             "tenant_id" varchar(50) COLLATE "pg_catalog"."default",
                                             "create_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
                                             "create_by" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
                                             "create_time" timestamp(6) NOT NULL,
                                             "process_id" int8 NOT NULL,
                                             "priority" int2,
                                             "instance_no" varchar(50) COLLATE "pg_catalog"."default",
                                             "business_key" varchar(100) COLLATE "pg_catalog"."default",
                                             "variable" text COLLATE "pg_catalog"."default",
                                             "instance_version" int4,
                                             "expire_time" timestamp(6),
                                             "last_update_by" varchar(50) COLLATE "pg_catalog"."default",
                                             "last_update_time" timestamp(6),
                                             "instance_state" int2 NOT NULL DEFAULT 0,
                                             "end_time" timestamp(6)
)
;
COMMENT ON COLUMN "public"."flw_his_instance"."id" IS '主键ID';
COMMENT ON COLUMN "public"."flw_his_instance"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "public"."flw_his_instance"."create_id" IS '创建人ID';
COMMENT ON COLUMN "public"."flw_his_instance"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."flw_his_instance"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."flw_his_instance"."process_id" IS '流程定义ID';
COMMENT ON COLUMN "public"."flw_his_instance"."priority" IS '优先级';
COMMENT ON COLUMN "public"."flw_his_instance"."instance_no" IS '流程实例编号';
COMMENT ON COLUMN "public"."flw_his_instance"."business_key" IS '业务KEY';
COMMENT ON COLUMN "public"."flw_his_instance"."variable" IS '变量json';
COMMENT ON COLUMN "public"."flw_his_instance"."instance_version" IS '流程实例版本';
COMMENT ON COLUMN "public"."flw_his_instance"."expire_time" IS '期望完成时间';
COMMENT ON COLUMN "public"."flw_his_instance"."last_update_by" IS '上次更新人';
COMMENT ON COLUMN "public"."flw_his_instance"."last_update_time" IS '上次更新时间';
COMMENT ON COLUMN "public"."flw_his_instance"."instance_state" IS '状态 0，活动 1，结束';
COMMENT ON COLUMN "public"."flw_his_instance"."end_time" IS '结束时间';
COMMENT ON TABLE "public"."flw_his_instance" IS '历史流程实例表';

-- ----------------------------
-- Table structure for flw_his_task
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_his_task";
CREATE TABLE "public"."flw_his_task" (
                                         "id" int8 NOT NULL,
                                         "tenant_id" varchar(50) COLLATE "pg_catalog"."default",
                                         "create_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
                                         "create_by" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
                                         "create_time" timestamp(6) NOT NULL,
                                         "instance_id" int8 NOT NULL,
                                         "parent_task_id" int8,
                                         "task_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
                                         "display_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
                                         "task_type" int2 NOT NULL,
                                         "perform_type" int2,
                                         "action_url" varchar(200) COLLATE "pg_catalog"."default",
                                         "variable" text COLLATE "pg_catalog"."default",
                                         "assignor_id" varchar(100) COLLATE "pg_catalog"."default",
                                         "assignor" varchar(100) COLLATE "pg_catalog"."default",
                                         "expire_time" timestamp(6),
                                         "remind_time" timestamp(6),
                                         "remind_repeat" int2 NOT NULL,
                                         "viewed" int2 NOT NULL DEFAULT 0,
                                         "finish_time" timestamp(6),
                                         "task_state" int2 NOT NULL DEFAULT 0
)
;
COMMENT ON COLUMN "public"."flw_his_task"."id" IS '主键ID';
COMMENT ON COLUMN "public"."flw_his_task"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "public"."flw_his_task"."create_id" IS '创建人ID';
COMMENT ON COLUMN "public"."flw_his_task"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."flw_his_task"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."flw_his_task"."instance_id" IS '流程实例ID';
COMMENT ON COLUMN "public"."flw_his_task"."parent_task_id" IS '父任务ID';
COMMENT ON COLUMN "public"."flw_his_task"."task_name" IS '任务名称';
COMMENT ON COLUMN "public"."flw_his_task"."display_name" IS '任务显示名称';
COMMENT ON COLUMN "public"."flw_his_task"."task_type" IS '任务类型';
COMMENT ON COLUMN "public"."flw_his_task"."perform_type" IS '参与类型';
COMMENT ON COLUMN "public"."flw_his_task"."action_url" IS '任务处理的url';
COMMENT ON COLUMN "public"."flw_his_task"."variable" IS '变量json';
COMMENT ON COLUMN "public"."flw_his_task"."assignor_id" IS '委托人ID';
COMMENT ON COLUMN "public"."flw_his_task"."assignor" IS '委托人';
COMMENT ON COLUMN "public"."flw_his_task"."expire_time" IS '任务期望完成时间';
COMMENT ON COLUMN "public"."flw_his_task"."remind_time" IS '提醒时间';
COMMENT ON COLUMN "public"."flw_his_task"."remind_repeat" IS '提醒次数';
COMMENT ON COLUMN "public"."flw_his_task"."viewed" IS '已阅 0，否 1，是';
COMMENT ON COLUMN "public"."flw_his_task"."finish_time" IS '任务完成时间';
COMMENT ON COLUMN "public"."flw_his_task"."task_state" IS '任务状态 0，活动 1，结束 2，超时 3，终止';
COMMENT ON TABLE "public"."flw_his_task" IS '历史任务表';

-- ----------------------------
-- Table structure for flw_his_task_actor
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_his_task_actor";
CREATE TABLE "public"."flw_his_task_actor" (
                                               "id" int8 NOT NULL,
                                               "tenant_id" varchar(50) COLLATE "pg_catalog"."default",
                                               "instance_id" int8 NOT NULL,
                                               "task_id" int8 NOT NULL,
                                               "actor_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
                                               "actor_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
                                               "actor_type" int4 NOT NULL,
                                               "weight" int4
)
;
COMMENT ON COLUMN "public"."flw_his_task_actor"."id" IS '主键 ID';
COMMENT ON COLUMN "public"."flw_his_task_actor"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "public"."flw_his_task_actor"."instance_id" IS '流程实例ID';
COMMENT ON COLUMN "public"."flw_his_task_actor"."task_id" IS '任务ID';
COMMENT ON COLUMN "public"."flw_his_task_actor"."actor_id" IS '参与者ID';
COMMENT ON COLUMN "public"."flw_his_task_actor"."actor_name" IS '参与者名称';
COMMENT ON COLUMN "public"."flw_his_task_actor"."actor_type" IS '参与者类型 0，用户 1，角色 2，部门';
COMMENT ON COLUMN "public"."flw_his_task_actor"."weight" IS '票签权重';
COMMENT ON TABLE "public"."flw_his_task_actor" IS '历史任务参与者表';

-- ----------------------------
-- Table structure for flw_instance
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_instance";
CREATE TABLE "public"."flw_instance" (
                                         "id" int8 NOT NULL,
                                         "tenant_id" varchar(50) COLLATE "pg_catalog"."default",
                                         "create_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
                                         "create_by" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
                                         "create_time" timestamp(6) NOT NULL,
                                         "process_id" int8 NOT NULL,
                                         "priority" int2,
                                         "instance_no" varchar(50) COLLATE "pg_catalog"."default",
                                         "business_key" varchar(100) COLLATE "pg_catalog"."default",
                                         "variable" text COLLATE "pg_catalog"."default",
                                         "instance_version" int4,
                                         "expire_time" timestamp(6),
                                         "last_update_by" varchar(50) COLLATE "pg_catalog"."default",
                                         "last_update_time" timestamp(6)
)
;
COMMENT ON COLUMN "public"."flw_instance"."id" IS '主键ID';
COMMENT ON COLUMN "public"."flw_instance"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "public"."flw_instance"."create_id" IS '创建人ID';
COMMENT ON COLUMN "public"."flw_instance"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."flw_instance"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."flw_instance"."process_id" IS '流程定义ID';
COMMENT ON COLUMN "public"."flw_instance"."priority" IS '优先级';
COMMENT ON COLUMN "public"."flw_instance"."instance_no" IS '流程实例编号';
COMMENT ON COLUMN "public"."flw_instance"."business_key" IS '业务KEY';
COMMENT ON COLUMN "public"."flw_instance"."variable" IS '变量json';
COMMENT ON COLUMN "public"."flw_instance"."instance_version" IS '流程实例版本';
COMMENT ON COLUMN "public"."flw_instance"."expire_time" IS '期望完成时间';
COMMENT ON COLUMN "public"."flw_instance"."last_update_by" IS '上次更新人';
COMMENT ON COLUMN "public"."flw_instance"."last_update_time" IS '上次更新时间';
COMMENT ON TABLE "public"."flw_instance" IS '流程实例表';


-- ----------------------------
-- Table structure for flw_task
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_task";
CREATE TABLE "public"."flw_task" (
                                     "id" int8 NOT NULL,
                                     "tenant_id" varchar(50) COLLATE "pg_catalog"."default",
                                     "create_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
                                     "create_by" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
                                     "create_time" timestamp(6) NOT NULL,
                                     "instance_id" int8 NOT NULL,
                                     "parent_task_id" int8,
                                     "task_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
                                     "display_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
                                     "task_type" int2 NOT NULL,
                                     "perform_type" int2,
                                     "action_url" varchar(200) COLLATE "pg_catalog"."default",
                                     "variable" text COLLATE "pg_catalog"."default",
                                     "assignor_id" varchar(100) COLLATE "pg_catalog"."default",
                                     "assignor" varchar(100) COLLATE "pg_catalog"."default",
                                     "expire_time" timestamp(6),
                                     "remind_time" timestamp(6),
                                     "remind_repeat" int2 NOT NULL DEFAULT 0,
                                     "viewed" int2 NOT NULL DEFAULT 0,
                                     "finish_time" timestamp(6)
)
;
COMMENT ON COLUMN "public"."flw_task"."id" IS '主键ID';
COMMENT ON COLUMN "public"."flw_task"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "public"."flw_task"."create_id" IS '创建人ID';
COMMENT ON COLUMN "public"."flw_task"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."flw_task"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."flw_task"."instance_id" IS '流程实例ID';
COMMENT ON COLUMN "public"."flw_task"."parent_task_id" IS '父任务ID';
COMMENT ON COLUMN "public"."flw_task"."task_name" IS '任务名称';
COMMENT ON COLUMN "public"."flw_task"."display_name" IS '任务显示名称';
COMMENT ON COLUMN "public"."flw_task"."task_type" IS '任务类型';
COMMENT ON COLUMN "public"."flw_task"."perform_type" IS '参与类型';
COMMENT ON COLUMN "public"."flw_task"."action_url" IS '任务处理的url';
COMMENT ON COLUMN "public"."flw_task"."variable" IS '变量json';
COMMENT ON COLUMN "public"."flw_task"."assignor_id" IS '委托人ID';
COMMENT ON COLUMN "public"."flw_task"."assignor" IS '委托人';
COMMENT ON COLUMN "public"."flw_task"."expire_time" IS '任务期望完成时间';
COMMENT ON COLUMN "public"."flw_task"."remind_time" IS '提醒时间';
COMMENT ON COLUMN "public"."flw_task"."remind_repeat" IS '提醒次数';
COMMENT ON COLUMN "public"."flw_task"."viewed" IS '已阅 0，否 1，是';
COMMENT ON COLUMN "public"."flw_task"."finish_time" IS '完成时间';
COMMENT ON TABLE "public"."flw_task" IS '任务表';

-- ----------------------------
-- Table structure for flw_task_actor
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_task_actor";
CREATE TABLE "public"."flw_task_actor" (
                                           "id" int8 NOT NULL,
                                           "tenant_id" varchar(50) COLLATE "pg_catalog"."default",
                                           "instance_id" int8 NOT NULL,
                                           "task_id" int8 NOT NULL,
                                           "actor_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
                                           "actor_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
                                           "actor_type" int4 NOT NULL,
                                           "weight" int4
)
;
COMMENT ON COLUMN "public"."flw_task_actor"."id" IS '主键 ID';
COMMENT ON COLUMN "public"."flw_task_actor"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "public"."flw_task_actor"."instance_id" IS '流程实例ID';
COMMENT ON COLUMN "public"."flw_task_actor"."task_id" IS '任务ID';
COMMENT ON COLUMN "public"."flw_task_actor"."actor_id" IS '参与者ID';
COMMENT ON COLUMN "public"."flw_task_actor"."actor_name" IS '参与者名称';
COMMENT ON COLUMN "public"."flw_task_actor"."actor_type" IS '参与者类型 0，用户 1，角色 2，部门';
COMMENT ON COLUMN "public"."flw_task_actor"."weight" IS '票签权重';
COMMENT ON TABLE "public"."flw_task_actor" IS '任务参与者表';

-- ----------------------------
-- Table structure for flw_task_cc
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_task_cc";
CREATE TABLE "public"."flw_task_cc" (
                                        "id" int8 NOT NULL,
                                        "tenant_id" varchar(50) COLLATE "pg_catalog"."default",
                                        "create_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
                                        "create_by" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
                                        "create_time" timestamp(6) NOT NULL,
                                        "instance_id" int8 NOT NULL,
                                        "parent_task_id" int8,
                                        "task_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
                                        "display_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
                                        "actor_id" varchar(300) COLLATE "pg_catalog"."default" NOT NULL,
                                        "actor_name" varchar(300) COLLATE "pg_catalog"."default" NOT NULL,
                                        "task_type" int4 NOT NULL,
                                        "task_state" int2 NOT NULL,
                                        "finish_time" timestamp(6)
)
;
COMMENT ON COLUMN "public"."flw_task_cc"."id" IS '主键ID';
COMMENT ON COLUMN "public"."flw_task_cc"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "public"."flw_task_cc"."create_id" IS '创建人ID';
COMMENT ON COLUMN "public"."flw_task_cc"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."flw_task_cc"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."flw_task_cc"."instance_id" IS '流程实例ID';
COMMENT ON COLUMN "public"."flw_task_cc"."parent_task_id" IS '父任务ID';
COMMENT ON COLUMN "public"."flw_task_cc"."task_name" IS '任务名称';
COMMENT ON COLUMN "public"."flw_task_cc"."display_name" IS '任务显示名称';
COMMENT ON COLUMN "public"."flw_task_cc"."actor_id" IS '参与者ID';
COMMENT ON COLUMN "public"."flw_task_cc"."actor_name" IS '参与者名称';
COMMENT ON COLUMN "public"."flw_task_cc"."task_type" IS '任务类型 0，用户 1，角色 2，部门';
COMMENT ON COLUMN "public"."flw_task_cc"."task_state" IS '任务状态 0，结束 1，活动';
COMMENT ON COLUMN "public"."flw_task_cc"."finish_time" IS '完成时间';
COMMENT ON TABLE "public"."flw_task_cc" IS '抄送任务表';

-- ----------------------------
-- Indexes structure for table flw_his_instance
-- ----------------------------
CREATE INDEX "idx_his_instance_process_id" ON "public"."flw_his_instance" USING btree (
    "process_id" "pg_catalog"."int8_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table flw_his_instance
-- ----------------------------
ALTER TABLE "public"."flw_his_instance" ADD CONSTRAINT "flw_his_instance_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table flw_his_task
-- ----------------------------
CREATE INDEX "idx_his_task_instance_id" ON "public"."flw_his_task" USING btree (
    "instance_id" "pg_catalog"."int8_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_his_task_parent_task_id" ON "public"."flw_his_task" USING btree (
    "parent_task_id" "pg_catalog"."int8_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table flw_his_task
-- ----------------------------
ALTER TABLE "public"."flw_his_task" ADD CONSTRAINT "flw_his_task_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table flw_his_task_actor
-- ----------------------------
CREATE INDEX "idx_his_task_actor_task_id" ON "public"."flw_his_task_actor" USING btree (
    "task_id" "pg_catalog"."int8_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table flw_his_task_actor
-- ----------------------------
ALTER TABLE "public"."flw_his_task_actor" ADD CONSTRAINT "flw_his_task_actor_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table flw_instance
-- ----------------------------
CREATE INDEX "idx_instance_process_id" ON "public"."flw_instance" USING btree (
    "process_id" "pg_catalog"."int8_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table flw_instance
-- ----------------------------
ALTER TABLE "public"."flw_instance" ADD CONSTRAINT "flw_instance_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table flw_process
-- ----------------------------
CREATE INDEX "idx_process_name" ON "public"."flw_process" USING btree (
    "process_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table flw_process
-- ----------------------------
ALTER TABLE "public"."flw_process" ADD CONSTRAINT "flw_process_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table flw_task
-- ----------------------------
CREATE INDEX "idx_task_instance_id" ON "public"."flw_task" USING btree (
    "instance_id" "pg_catalog"."int8_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table flw_task
-- ----------------------------
ALTER TABLE "public"."flw_task" ADD CONSTRAINT "flw_task_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table flw_task_actor
-- ----------------------------
CREATE INDEX "idx_task_actor_task_id" ON "public"."flw_task_actor" USING btree (
    "task_id" "pg_catalog"."int8_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table flw_task_actor
-- ----------------------------
ALTER TABLE "public"."flw_task_actor" ADD CONSTRAINT "flw_task_actor_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table flw_task_cc
-- ----------------------------
ALTER TABLE "public"."flw_task_cc" ADD CONSTRAINT "flw_task_cc_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table flw_his_instance
-- ----------------------------
ALTER TABLE "public"."flw_his_instance" ADD CONSTRAINT "flw_his_instance_process_id_fkey" FOREIGN KEY ("process_id") REFERENCES "public"."flw_process" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table flw_his_task
-- ----------------------------
ALTER TABLE "public"."flw_his_task" ADD CONSTRAINT "flw_his_task_instance_id_fkey" FOREIGN KEY ("instance_id") REFERENCES "public"."flw_his_instance" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table flw_his_task_actor
-- ----------------------------
ALTER TABLE "public"."flw_his_task_actor" ADD CONSTRAINT "flw_his_task_actor_task_id_fkey" FOREIGN KEY ("task_id") REFERENCES "public"."flw_his_task" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table flw_instance
-- ----------------------------
ALTER TABLE "public"."flw_instance" ADD CONSTRAINT "flw_instance_process_id_fkey" FOREIGN KEY ("process_id") REFERENCES "public"."flw_process" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table flw_task
-- ----------------------------
ALTER TABLE "public"."flw_task" ADD CONSTRAINT "flw_task_instance_id_fkey" FOREIGN KEY ("instance_id") REFERENCES "public"."flw_instance" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table flw_task_actor
-- ----------------------------
ALTER TABLE "public"."flw_task_actor" ADD CONSTRAINT "flw_task_actor_task_id_fkey" FOREIGN KEY ("task_id") REFERENCES "public"."flw_task" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
