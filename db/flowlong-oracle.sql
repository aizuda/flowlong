-- ----------------------------
-- Table structure for flw_ext_instance
-- ----------------------------
DROP TABLE "flw_ext_instance";
CREATE TABLE "flw_ext_instance" (
                                           "id" NUMBER(20,0) NOT NULL,
                                           "tenant_id" NVARCHAR2(50),
                                           "process_id" NUMBER(20,0) NOT NULL,
                                           "model_content" NCLOB
)
    LOGGING
NOCOMPRESS
PCTFREE 10
INITRANS 1
STORAGE (
  BUFFER_POOL DEFAULT
)
PARALLEL 1
NOCACHE
DISABLE ROW MOVEMENT
;
COMMENT ON COLUMN "flw_ext_instance"."id" IS '主键ID';
COMMENT ON COLUMN "flw_ext_instance"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "flw_ext_instance"."process_id" IS '流程定义ID';
COMMENT ON COLUMN "flw_ext_instance"."model_content" IS '流程模型定义JSON内容';
COMMENT ON TABLE "flw_ext_instance" IS '扩展流程实例表';

-- ----------------------------
-- Records of flw_ext_instance
-- ----------------------------
COMMIT;
COMMIT;

-- ----------------------------
-- Table structure for flw_his_instance
-- ----------------------------
DROP TABLE "flw_his_instance";
CREATE TABLE "flw_his_instance" (
                                           "id" NUMBER(20,0) NOT NULL,
                                           "tenant_id" NVARCHAR2(50),
                                           "create_id" NVARCHAR2(50) NOT NULL,
                                           "create_by" NVARCHAR2(50) NOT NULL,
                                           "create_time" DATE NOT NULL,
                                           "process_id" NUMBER(20,0) NOT NULL,
                                           "parent_instance_id" NUMBER(20,0),
                                           "priority" NUMBER(4,0),
                                           "instance_no" NVARCHAR2(50),
                                           "business_key" NVARCHAR2(100),
                                           "variable" NCLOB,
                                           "current_node_name" NVARCHAR2(100) NOT NULL,
                                           "current_node_key" NVARCHAR2(100) NOT NULL,
                                           "expire_time" DATE,
                                           "last_update_by" NVARCHAR2(50),
                                           "last_update_time" DATE,
                                           "instance_state" NUMBER(4,0) NOT NULL,
                                           "end_time" DATE,
                                           "duration" NUMBER(20,0)
)
    LOGGING
NOCOMPRESS
PCTFREE 10
INITRANS 1
STORAGE (
  BUFFER_POOL DEFAULT
)
PARALLEL 1
NOCACHE
DISABLE ROW MOVEMENT
;
COMMENT ON COLUMN "flw_his_instance"."id" IS '主键ID';
COMMENT ON COLUMN "flw_his_instance"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "flw_his_instance"."create_id" IS '创建人ID';
COMMENT ON COLUMN "flw_his_instance"."create_by" IS '创建人名称';
COMMENT ON COLUMN "flw_his_instance"."create_time" IS '创建时间';
COMMENT ON COLUMN "flw_his_instance"."process_id" IS '流程定义ID';
COMMENT ON COLUMN "flw_his_instance"."parent_instance_id" IS '父流程实例ID';
COMMENT ON COLUMN "flw_his_instance"."priority" IS '优先级';
COMMENT ON COLUMN "flw_his_instance"."instance_no" IS '流程实例编号';
COMMENT ON COLUMN "flw_his_instance"."business_key" IS '业务KEY';
COMMENT ON COLUMN "flw_his_instance"."variable" IS '变量json';
COMMENT ON COLUMN "flw_his_instance"."current_node_name" IS '当前所在节点名称';
COMMENT ON COLUMN "flw_his_instance"."current_node_key" IS '当前所在节点key';
COMMENT ON COLUMN "flw_his_instance"."expire_time" IS '期望完成时间';
COMMENT ON COLUMN "flw_his_instance"."last_update_by" IS '上次更新人';
COMMENT ON COLUMN "flw_his_instance"."last_update_time" IS '上次更新时间';
COMMENT ON COLUMN "flw_his_instance"."instance_state" IS '状态 0，审批中 1，审批通过 2，审批拒绝 3，撤销审批 4，超时结束 5，强制终止';
COMMENT ON COLUMN "flw_his_instance"."end_time" IS '结束时间';
COMMENT ON COLUMN "flw_his_instance"."duration" IS '处理耗时';
COMMENT ON TABLE "flw_his_instance" IS '历史流程实例表';

-- ----------------------------
-- Records of flw_his_instance
-- ----------------------------
COMMIT;
COMMIT;

-- ----------------------------
-- Table structure for flw_his_task
-- ----------------------------
DROP TABLE "flw_his_task";
CREATE TABLE "flw_his_task" (
                                       "id" NUMBER(20,0) NOT NULL,
                                       "tenant_id" NVARCHAR2(50),
                                       "create_id" NVARCHAR2(50) NOT NULL,
                                       "create_by" NVARCHAR2(50) NOT NULL,
                                       "create_time" DATE NOT NULL,
                                       "instance_id" NUMBER(20,0) NOT NULL,
                                       "parent_task_id" NUMBER(20,0),
                                       "call_process_id" NUMBER(20,0),
                                       "call_instance_id" NUMBER(20,0),
                                       "task_name" NVARCHAR2(100) NOT NULL,
                                       "task_key" NVARCHAR2(100) NOT NULL,
                                       "task_type" NUMBER(4,0) NOT NULL,
                                       "perform_type" NUMBER(4,0),
                                       "action_url" NVARCHAR2(200),
                                       "variable" CLOB,
                                       "assignor_id" NVARCHAR2(100),
                                       "assignor" NVARCHAR2(255),
                                       "expire_time" DATE,
                                       "remind_time" DATE,
                                       "remind_repeat" NUMBER(4,0) NOT NULL,
                                       "viewed" NUMBER(4,0) NOT NULL,
                                       "finish_time" DATE,
                                       "task_state" NUMBER(4,0) NOT NULL,
                                       "duration" NUMBER(20,0)
)
    LOGGING
NOCOMPRESS
PCTFREE 10
INITRANS 1
STORAGE (
  BUFFER_POOL DEFAULT
)
PARALLEL 1
NOCACHE
DISABLE ROW MOVEMENT
;
COMMENT ON COLUMN "flw_his_task"."id" IS '主键ID';
COMMENT ON COLUMN "flw_his_task"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "flw_his_task"."create_id" IS '创建人ID';
COMMENT ON COLUMN "flw_his_task"."create_by" IS '创建人名称';
COMMENT ON COLUMN "flw_his_task"."create_time" IS '创建时间';
COMMENT ON COLUMN "flw_his_task"."instance_id" IS '流程实例ID';
COMMENT ON COLUMN "flw_his_task"."parent_task_id" IS '父任务ID';
COMMENT ON COLUMN "flw_his_task"."call_process_id" IS '调用外部流程定义ID';
COMMENT ON COLUMN "flw_his_task"."call_instance_id" IS '调用外部流程实例ID';
COMMENT ON COLUMN "flw_his_task"."task_name" IS '任务名称';
COMMENT ON COLUMN "flw_his_task"."task_key" IS '任务 key 唯一标识';
COMMENT ON COLUMN "flw_his_task"."task_type" IS '任务类型';
COMMENT ON COLUMN "flw_his_task"."perform_type" IS '参与类型';
COMMENT ON COLUMN "flw_his_task"."action_url" IS '任务处理的url';
COMMENT ON COLUMN "flw_his_task"."variable" IS '变量json';
COMMENT ON COLUMN "flw_his_task"."assignor_id" IS '委托人ID';
COMMENT ON COLUMN "flw_his_task"."assignor" IS '委托人';
COMMENT ON COLUMN "flw_his_task"."expire_time" IS '任务期望完成时间';
COMMENT ON COLUMN "flw_his_task"."remind_time" IS '提醒时间';
COMMENT ON COLUMN "flw_his_task"."remind_repeat" IS '提醒次数';
COMMENT ON COLUMN "flw_his_task"."viewed" IS '已阅 0，否 1，是';
COMMENT ON COLUMN "flw_his_task"."finish_time" IS '任务完成时间';
COMMENT ON COLUMN "flw_his_task"."task_state" IS '任务状态 0，活动 1，跳转 2，完成 3，拒绝 4，撤销审批  5，超时 6，终止 7，驳回终止';
COMMENT ON COLUMN "flw_his_task"."duration" IS '处理耗时';
COMMENT ON TABLE "flw_his_task" IS '历史任务表';

-- ----------------------------
-- Records of flw_his_task
-- ----------------------------
COMMIT;
COMMIT;

-- ----------------------------
-- Table structure for flw_his_task_actor
-- ----------------------------
DROP TABLE "flw_his_task_actor";
CREATE TABLE "flw_his_task_actor" (
                                             "id" NUMBER(20,0) NOT NULL,
                                             "tenant_id" NVARCHAR2(50),
                                             "instance_id" NUMBER(20,0) NOT NULL,
                                             "task_id" NUMBER(20,0) NOT NULL,
                                             "actor_id" NVARCHAR2(100) NOT NULL,
                                             "actor_name" NVARCHAR2(100) NOT NULL,
                                             "actor_type" NUMBER(11,0) NOT NULL,
                                             "weight" NUMBER(11,0)
)
    LOGGING
NOCOMPRESS
PCTFREE 10
INITRANS 1
STORAGE (
  BUFFER_POOL DEFAULT
)
PARALLEL 1
NOCACHE
DISABLE ROW MOVEMENT
;
COMMENT ON COLUMN "flw_his_task_actor"."id" IS '主键 ID';
COMMENT ON COLUMN "flw_his_task_actor"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "flw_his_task_actor"."instance_id" IS '流程实例ID';
COMMENT ON COLUMN "flw_his_task_actor"."task_id" IS '任务ID';
COMMENT ON COLUMN "flw_his_task_actor"."actor_id" IS '参与者ID';
COMMENT ON COLUMN "flw_his_task_actor"."actor_name" IS '参与者名称';
COMMENT ON COLUMN "flw_his_task_actor"."actor_type" IS '参与者类型 0，用户 1，角色 2，部门';
COMMENT ON COLUMN "flw_his_task_actor"."weight" IS '权重，票签任务时，该值为不同处理人员的分量比例，代理任务时，该值为 1 时为代理人';
COMMENT ON TABLE "flw_his_task_actor" IS '历史任务参与者表';

-- ----------------------------
-- Records of flw_his_task_actor
-- ----------------------------
COMMIT;
COMMIT;

-- ----------------------------
-- Table structure for flw_instance
-- ----------------------------
DROP TABLE "flw_instance";
CREATE TABLE "flw_instance" (
                                       "id" NUMBER(20,0) NOT NULL,
                                       "tenant_id" NVARCHAR2(50),
                                       "create_id" NVARCHAR2(50) NOT NULL,
                                       "create_by" NVARCHAR2(50) NOT NULL,
                                       "create_time" DATE NOT NULL,
                                       "process_id" NUMBER(20,0) NOT NULL,
                                       "parent_instance_id" NUMBER(20,0),
                                       "priority" NUMBER(4,0),
                                       "instance_no" NVARCHAR2(50),
                                       "business_key" NVARCHAR2(100),
                                       "variable" NCLOB,
                                       "current_node_name" NVARCHAR2(100) NOT NULL,
                                       "current_node_key" NVARCHAR2(100) NOT NULL,
                                       "expire_time" DATE,
                                       "last_update_by" NVARCHAR2(50),
                                       "last_update_time" DATE
)
    LOGGING
NOCOMPRESS
PCTFREE 10
INITRANS 1
STORAGE (
  BUFFER_POOL DEFAULT
)
PARALLEL 1
NOCACHE
DISABLE ROW MOVEMENT
;
COMMENT ON COLUMN "flw_instance"."id" IS '主键ID';
COMMENT ON COLUMN "flw_instance"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "flw_instance"."create_id" IS '创建人ID';
COMMENT ON COLUMN "flw_instance"."create_by" IS '创建人名称';
COMMENT ON COLUMN "flw_instance"."create_time" IS '创建时间';
COMMENT ON COLUMN "flw_instance"."process_id" IS '流程定义ID';
COMMENT ON COLUMN "flw_instance"."parent_instance_id" IS '父流程实例ID';
COMMENT ON COLUMN "flw_instance"."priority" IS '优先级';
COMMENT ON COLUMN "flw_instance"."instance_no" IS '流程实例编号';
COMMENT ON COLUMN "flw_instance"."business_key" IS '业务KEY';
COMMENT ON COLUMN "flw_instance"."variable" IS '变量json';
COMMENT ON COLUMN "flw_instance"."current_node_name" IS '当前所在节点名称';
COMMENT ON COLUMN "flw_instance"."current_node_key" IS '当前所在节点key';
COMMENT ON COLUMN "flw_instance"."expire_time" IS '期望完成时间';
COMMENT ON COLUMN "flw_instance"."last_update_by" IS '上次更新人';
COMMENT ON COLUMN "flw_instance"."last_update_time" IS '上次更新时间';
COMMENT ON TABLE "flw_instance" IS '流程实例表';

-- ----------------------------
-- Records of flw_instance
-- ----------------------------
COMMIT;
COMMIT;

-- ----------------------------
-- Table structure for flw_process
-- ----------------------------
DROP TABLE "flw_process";
CREATE TABLE "flw_process" (
                                      "id" NUMBER(20,0) NOT NULL,
                                      "tenant_id" NVARCHAR2(50),
                                      "create_id" NVARCHAR2(50) NOT NULL,
                                      "create_by" NVARCHAR2(50) NOT NULL,
                                      "create_time" DATE NOT NULL,
                                      "process_key" NVARCHAR2(100) NOT NULL,
                                      "process_name" NVARCHAR2(100) NOT NULL,
                                      "process_icon" NVARCHAR2(255),
                                      "process_type" NVARCHAR2(100),
                                      "process_version" NUMBER(11,0) NOT NULL,
                                      "instance_url" NVARCHAR2(200),
                                      "remark" NVARCHAR2(255),
                                      "use_scope" NUMBER(4,0) NOT NULL,
                                      "process_state" NUMBER(4,0) NOT NULL,
                                      "model_content" NCLOB,
                                      "sort" NUMBER(4,0)
)
    LOGGING
NOCOMPRESS
PCTFREE 10
INITRANS 1
STORAGE (
  BUFFER_POOL DEFAULT
)
PARALLEL 1
NOCACHE
DISABLE ROW MOVEMENT
;
COMMENT ON COLUMN "flw_process"."id" IS '主键ID';
COMMENT ON COLUMN "flw_process"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "flw_process"."create_id" IS '创建人ID';
COMMENT ON COLUMN "flw_process"."create_by" IS '创建人名称';
COMMENT ON COLUMN "flw_process"."create_time" IS '创建时间';
COMMENT ON COLUMN "flw_process"."process_key" IS '流程定义 key 唯一标识';
COMMENT ON COLUMN "flw_process"."process_name" IS '流程定义名称';
COMMENT ON COLUMN "flw_process"."process_icon" IS '流程图标地址';
COMMENT ON COLUMN "flw_process"."process_type" IS '流程类型';
COMMENT ON COLUMN "flw_process"."process_version" IS '流程版本，默认 1';
COMMENT ON COLUMN "flw_process"."instance_url" IS '实例地址';
COMMENT ON COLUMN "flw_process"."remark" IS '备注说明';
COMMENT ON COLUMN "flw_process"."use_scope" IS '使用范围 0，全员 1，指定人员（业务关联） 2，均不可提交';
COMMENT ON COLUMN "flw_process"."process_state" IS '流程状态 0，不可用 1，可用 2，历史版本';
COMMENT ON COLUMN "flw_process"."model_content" IS '流程模型定义JSON内容';
COMMENT ON COLUMN "flw_process"."sort" IS '排序';
COMMENT ON TABLE "flw_process" IS '流程定义表';

-- ----------------------------
-- Records of flw_process
-- ----------------------------
COMMIT;
COMMIT;

-- ----------------------------
-- Table structure for flw_task
-- ----------------------------
DROP TABLE "flw_task";
CREATE TABLE "flw_task" (
                                   "id" NUMBER(20,0) NOT NULL,
                                   "tenant_id" NVARCHAR2(50),
                                   "create_id" NVARCHAR2(50) NOT NULL,
                                   "create_by" NVARCHAR2(50) NOT NULL,
                                   "create_time" DATE NOT NULL,
                                   "instance_id" NUMBER(20,0) NOT NULL,
                                   "parent_task_id" NUMBER(20,0),
                                   "task_name" NVARCHAR2(100) NOT NULL,
                                   "task_key" NVARCHAR2(100) NOT NULL,
                                   "task_type" NUMBER(4,0) NOT NULL,
                                   "perform_type" NUMBER(4,0),
                                   "action_url" NVARCHAR2(200),
                                   "variable" CLOB,
                                   "assignor_id" NVARCHAR2(100),
                                   "assignor" NVARCHAR2(255),
                                   "expire_time" DATE,
                                   "remind_time" DATE,
                                   "remind_repeat" NUMBER(4,0) NOT NULL,
                                   "viewed" NUMBER(4,0) NOT NULL
)
    LOGGING
NOCOMPRESS
PCTFREE 10
INITRANS 1
STORAGE (
  BUFFER_POOL DEFAULT
)
PARALLEL 1
NOCACHE
DISABLE ROW MOVEMENT
;
COMMENT ON COLUMN "flw_task"."id" IS '主键ID';
COMMENT ON COLUMN "flw_task"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "flw_task"."create_id" IS '创建人ID';
COMMENT ON COLUMN "flw_task"."create_by" IS '创建人名称';
COMMENT ON COLUMN "flw_task"."create_time" IS '创建时间';
COMMENT ON COLUMN "flw_task"."instance_id" IS '流程实例ID';
COMMENT ON COLUMN "flw_task"."parent_task_id" IS '父任务ID';
COMMENT ON COLUMN "flw_task"."task_name" IS '任务名称';
COMMENT ON COLUMN "flw_task"."task_key" IS '任务 key 唯一标识';
COMMENT ON COLUMN "flw_task"."task_type" IS '任务类型';
COMMENT ON COLUMN "flw_task"."perform_type" IS '参与类型';
COMMENT ON COLUMN "flw_task"."action_url" IS '任务处理的url';
COMMENT ON COLUMN "flw_task"."variable" IS '变量json';
COMMENT ON COLUMN "flw_task"."assignor_id" IS '委托人ID';
COMMENT ON COLUMN "flw_task"."assignor" IS '委托人';
COMMENT ON COLUMN "flw_task"."expire_time" IS '任务期望完成时间';
COMMENT ON COLUMN "flw_task"."remind_time" IS '提醒时间';
COMMENT ON COLUMN "flw_task"."remind_repeat" IS '提醒次数';
COMMENT ON COLUMN "flw_task"."viewed" IS '已阅 0，否 1，是';
COMMENT ON TABLE "flw_task" IS '任务表';

-- ----------------------------
-- Records of flw_task
-- ----------------------------
COMMIT;
COMMIT;

-- ----------------------------
-- Table structure for flw_task_actor
-- ----------------------------
DROP TABLE "flw_task_actor";
CREATE TABLE "flw_task_actor" (
                                         "id" NUMBER(20,0) NOT NULL,
                                         "tenant_id" NVARCHAR2(50),
                                         "instance_id" NUMBER(20,0) NOT NULL,
                                         "task_id" NUMBER(20,0) NOT NULL,
                                         "actor_id" NVARCHAR2(100) NOT NULL,
                                         "actor_name" NVARCHAR2(100) NOT NULL,
                                         "actor_type" NUMBER(11,0) NOT NULL,
                                         "weight" NUMBER(11,0)
)
    LOGGING
NOCOMPRESS
PCTFREE 10
INITRANS 1
STORAGE (
  BUFFER_POOL DEFAULT
)
PARALLEL 1
NOCACHE
DISABLE ROW MOVEMENT
;
COMMENT ON COLUMN "flw_task_actor"."id" IS '主键 ID';
COMMENT ON COLUMN "flw_task_actor"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "flw_task_actor"."instance_id" IS '流程实例ID';
COMMENT ON COLUMN "flw_task_actor"."task_id" IS '任务ID';
COMMENT ON COLUMN "flw_task_actor"."actor_id" IS '参与者ID';
COMMENT ON COLUMN "flw_task_actor"."actor_name" IS '参与者名称';
COMMENT ON COLUMN "flw_task_actor"."actor_type" IS '参与者类型 0，用户 1，角色 2，部门';
COMMENT ON COLUMN "flw_task_actor"."weight" IS '权重，票签任务时，该值为不同处理人员的分量比例，代理任务时，该值为 1 时为代理人';
COMMENT ON TABLE "flw_task_actor" IS '任务参与者表';

-- ----------------------------
-- Records of flw_task_actor
-- ----------------------------
COMMIT;
COMMIT;

-- ----------------------------
-- Primary Key structure for table flw_ext_instance
-- ----------------------------
ALTER TABLE "flw_ext_instance" ADD CONSTRAINT "SYS_C0012294" PRIMARY KEY ("id");

-- ----------------------------
-- Checks structure for table flw_ext_instance
-- ----------------------------
ALTER TABLE "flw_ext_instance" ADD CONSTRAINT "SYS_C0012236" CHECK ("id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_ext_instance" ADD CONSTRAINT "SYS_C0012238" CHECK ("process_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;

-- ----------------------------
-- Primary Key structure for table flw_his_instance
-- ----------------------------
ALTER TABLE "flw_his_instance" ADD CONSTRAINT "SYS_C0012296" PRIMARY KEY ("id");

-- ----------------------------
-- Checks structure for table flw_his_instance
-- ----------------------------
ALTER TABLE "flw_his_instance" ADD CONSTRAINT "SYS_C0012240" CHECK ("id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_instance" ADD CONSTRAINT "SYS_C0012243" CHECK ("create_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_instance" ADD CONSTRAINT "SYS_C0012246" CHECK ("create_by" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_instance" ADD CONSTRAINT "SYS_C0012249" CHECK ("create_time" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_instance" ADD CONSTRAINT "SYS_C0012251" CHECK ("process_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_instance" ADD CONSTRAINT "SYS_C0012253" CHECK ("current_node_name" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_instance" ADD CONSTRAINT "SYS_C0012255" CHECK ("current_node_key" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_instance" ADD CONSTRAINT "SYS_C0012257" CHECK ("instance_state" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;

-- ----------------------------
-- Indexes structure for table flw_his_instance
-- ----------------------------
CREATE INDEX "idx_his_instance_process_id"
    ON "flw_his_instance" ("process_id" ASC)
    LOGGING
  VISIBLE
PCTFREE 10
INITRANS 2
STORAGE (
  BUFFER_POOL DEFAULT
);

-- ----------------------------
-- Primary Key structure for table flw_his_task
-- ----------------------------
ALTER TABLE "flw_his_task" ADD CONSTRAINT "SYS_C0012297" PRIMARY KEY ("id");

-- ----------------------------
-- Checks structure for table flw_his_task
-- ----------------------------
ALTER TABLE "flw_his_task" ADD CONSTRAINT "SYS_C0012241" CHECK ("id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task" ADD CONSTRAINT "SYS_C0012244" CHECK ("create_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task" ADD CONSTRAINT "SYS_C0012247" CHECK ("create_by" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task" ADD CONSTRAINT "SYS_C0012250" CHECK ("create_time" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task" ADD CONSTRAINT "SYS_C0012252" CHECK ("instance_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task" ADD CONSTRAINT "SYS_C0012254" CHECK ("task_name" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task" ADD CONSTRAINT "SYS_C0012256" CHECK ("task_key" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task" ADD CONSTRAINT "SYS_C0012258" CHECK ("task_type" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task" ADD CONSTRAINT "SYS_C0012259" CHECK ("remind_repeat" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task" ADD CONSTRAINT "SYS_C0012260" CHECK ("viewed" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task" ADD CONSTRAINT "SYS_C0012261" CHECK ("task_state" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;

-- ----------------------------
-- Indexes structure for table flw_his_task
-- ----------------------------
CREATE INDEX "idx_his_task_instance_id"
    ON "flw_his_task" ("instance_id" ASC)
    LOGGING
  VISIBLE
PCTFREE 10
INITRANS 2
STORAGE (
  BUFFER_POOL DEFAULT
);
CREATE INDEX "idx_his_task_parent_task_id"
    ON "flw_his_task" ("parent_task_id" ASC)
    LOGGING
  VISIBLE
PCTFREE 10
INITRANS 2
STORAGE (
  BUFFER_POOL DEFAULT
);

-- ----------------------------
-- Primary Key structure for table flw_his_task_actor
-- ----------------------------
ALTER TABLE "flw_his_task_actor" ADD CONSTRAINT "SYS_C0012295" PRIMARY KEY ("id");

-- ----------------------------
-- Checks structure for table flw_his_task_actor
-- ----------------------------
ALTER TABLE "flw_his_task_actor" ADD CONSTRAINT "SYS_C0012235" CHECK ("id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task_actor" ADD CONSTRAINT "SYS_C0012237" CHECK ("instance_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task_actor" ADD CONSTRAINT "SYS_C0012239" CHECK ("task_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task_actor" ADD CONSTRAINT "SYS_C0012242" CHECK ("actor_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task_actor" ADD CONSTRAINT "SYS_C0012245" CHECK ("actor_name" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_his_task_actor" ADD CONSTRAINT "SYS_C0012248" CHECK ("actor_type" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;

-- ----------------------------
-- Indexes structure for table flw_his_task_actor
-- ----------------------------
CREATE INDEX "idx_his_task_actor_task_id"
    ON "flw_his_task_actor" ("task_id" ASC)
    LOGGING
  VISIBLE
PCTFREE 10
INITRANS 2
STORAGE (
  BUFFER_POOL DEFAULT
);

-- ----------------------------
-- Primary Key structure for table flw_instance
-- ----------------------------
ALTER TABLE "flw_instance" ADD CONSTRAINT "SYS_C0012298" PRIMARY KEY ("id");

-- ----------------------------
-- Checks structure for table flw_instance
-- ----------------------------
ALTER TABLE "flw_instance" ADD CONSTRAINT "SYS_C0012262" CHECK ("id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_instance" ADD CONSTRAINT "SYS_C0012263" CHECK ("create_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_instance" ADD CONSTRAINT "SYS_C0012264" CHECK ("create_by" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_instance" ADD CONSTRAINT "SYS_C0012265" CHECK ("create_time" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_instance" ADD CONSTRAINT "SYS_C0012266" CHECK ("process_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_instance" ADD CONSTRAINT "SYS_C0012267" CHECK ("current_node_name" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_instance" ADD CONSTRAINT "SYS_C0012268" CHECK ("current_node_key" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;

-- ----------------------------
-- Indexes structure for table flw_instance
-- ----------------------------
CREATE INDEX "idx_instance_process_id"
    ON "flw_instance" ("process_id" ASC)
    LOGGING
  VISIBLE
PCTFREE 10
INITRANS 2
STORAGE (
  BUFFER_POOL DEFAULT
);

-- ----------------------------
-- Primary Key structure for table flw_process
-- ----------------------------
ALTER TABLE "flw_process" ADD CONSTRAINT "SYS_C0012299" PRIMARY KEY ("id");

-- ----------------------------
-- Checks structure for table flw_process
-- ----------------------------
ALTER TABLE "flw_process" ADD CONSTRAINT "SYS_C0012269" CHECK ("id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_process" ADD CONSTRAINT "SYS_C0012270" CHECK ("create_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_process" ADD CONSTRAINT "SYS_C0012271" CHECK ("create_by" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_process" ADD CONSTRAINT "SYS_C0012272" CHECK ("create_time" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_process" ADD CONSTRAINT "SYS_C0012273" CHECK ("process_key" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_process" ADD CONSTRAINT "SYS_C0012274" CHECK ("process_name" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_process" ADD CONSTRAINT "SYS_C0012275" CHECK ("process_version" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_process" ADD CONSTRAINT "SYS_C0012276" CHECK ("use_scope" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_process" ADD CONSTRAINT "SYS_C0012277" CHECK ("process_state" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;

-- ----------------------------
-- Indexes structure for table flw_process
-- ----------------------------
CREATE INDEX "idx_process_name"
    ON "flw_process" ("process_name" ASC)
    LOGGING
  VISIBLE
PCTFREE 10
INITRANS 2
STORAGE (
  BUFFER_POOL DEFAULT
);

-- ----------------------------
-- Primary Key structure for table flw_task
-- ----------------------------
ALTER TABLE "flw_task" ADD CONSTRAINT "SYS_C0012300" PRIMARY KEY ("id");

-- ----------------------------
-- Checks structure for table flw_task
-- ----------------------------
ALTER TABLE "flw_task" ADD CONSTRAINT "SYS_C0012282" CHECK ("id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_task" ADD CONSTRAINT "SYS_C0012284" CHECK ("create_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_task" ADD CONSTRAINT "SYS_C0012286" CHECK ("create_by" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_task" ADD CONSTRAINT "SYS_C0012287" CHECK ("create_time" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_task" ADD CONSTRAINT "SYS_C0012288" CHECK ("instance_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_task" ADD CONSTRAINT "SYS_C0012289" CHECK ("task_name" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_task" ADD CONSTRAINT "SYS_C0012290" CHECK ("task_key" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_task" ADD CONSTRAINT "SYS_C0012291" CHECK ("task_type" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_task" ADD CONSTRAINT "SYS_C0012292" CHECK ("remind_repeat" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_task" ADD CONSTRAINT "SYS_C0012293" CHECK ("viewed" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;

-- ----------------------------
-- Indexes structure for table flw_task
-- ----------------------------
CREATE INDEX "idx_task_instance_id"
    ON "flw_task" ("instance_id" ASC)
    LOGGING
  VISIBLE
PCTFREE 10
INITRANS 2
STORAGE (
  BUFFER_POOL DEFAULT
);

-- ----------------------------
-- Primary Key structure for table flw_task_actor
-- ----------------------------
ALTER TABLE "flw_task_actor" ADD CONSTRAINT "SYS_C0012301" PRIMARY KEY ("id");

-- ----------------------------
-- Checks structure for table flw_task_actor
-- ----------------------------
ALTER TABLE "flw_task_actor" ADD CONSTRAINT "SYS_C0012278" CHECK ("id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_task_actor" ADD CONSTRAINT "SYS_C0012279" CHECK ("instance_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_task_actor" ADD CONSTRAINT "SYS_C0012280" CHECK ("task_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_task_actor" ADD CONSTRAINT "SYS_C0012281" CHECK ("actor_id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_task_actor" ADD CONSTRAINT "SYS_C0012283" CHECK ("actor_name" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
ALTER TABLE "flw_task_actor" ADD CONSTRAINT "SYS_C0012285" CHECK ("actor_type" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;

-- ----------------------------
-- Indexes structure for table flw_task_actor
-- ----------------------------
CREATE INDEX "idx_task_actor_task_id"
    ON "flw_task_actor" ("task_id" ASC)
    LOGGING
  VISIBLE
PCTFREE 10
INITRANS 2
STORAGE (
  BUFFER_POOL DEFAULT
);

-- ----------------------------
-- Foreign Keys structure for table flw_ext_instance
-- ----------------------------
ALTER TABLE "flw_ext_instance" ADD CONSTRAINT "fk_ext_instance_id" FOREIGN KEY ("id") REFERENCES "flw_his_instance" ("id") NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
