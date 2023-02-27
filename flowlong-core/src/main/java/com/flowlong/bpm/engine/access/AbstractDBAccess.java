/* Copyright 2023-2025 www.flowlong.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flowlong.bpm.engine.access;

import com.flowlong.bpm.engine.DBAccess;
import com.flowlong.bpm.engine.FlowLongException;
import com.flowlong.bpm.engine.access.dialect.Dialect;
import com.flowlong.bpm.engine.access.jdbc.JdbcHelper;
import com.flowlong.bpm.engine.assist.ClassUtils;
import com.flowlong.bpm.engine.assist.ConfigHelper;
import com.flowlong.bpm.engine.assist.StringUtils;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * 抽象数据库访问类
 * 封装SQL语句的构造
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
public abstract class AbstractDBAccess implements DBAccess {
    protected static final String KEY_SQL = "SQL";
    protected static final String KEY_ARGS = "ARGS";
    protected static final String KEY_type = "TYPE";
    protected static final String KEY_ENTITY = "ENTITY";
    protected static final String KEY_SU = "SU";
    protected static final String SAVE = "SAVE";
    protected static final String UPDATE = "UPDATE";
    protected static final String PROCESS_INSERT = "insert into flw_process (id,tenant_id,name,display_name,type,instance_url,state,version,create_time,creator) values (?,?,?,?,?,?,?,?,?,?)";
    protected static final String PROCESS_UPDATE = "update flw_process set name=?, display_name=?,state=?,instance_url=?,create_time=?,creator=? where id=? ";
    protected static final String PROCESS_DELETE = "delete from flw_process where id = ?";
    protected static final String PROCESS_UPDATE_BLOB = "update flw_process set content=? where id=?";
    protected static final String PROCESS_UPDATE_type = "update flw_process set type=? where id=?";
    protected static final String INSTANCE_INSERT = "insert into flw_instance (id,tenant_id,process_id,creator,create_time,parent_id,parent_node_name,expire_time,last_Update_time,last_updator,instance_no,variable,version) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
    protected static final String INSTANCE_UPDATE = "update flw_instance set last_updator=?, last_Update_time=?, variable = ?, expire_time=?, version = version + 1 where id=? and version = ?";
    protected static final String INSTANCE_DELETE = "delete from flw_instance where id = ?";
    protected static final String INSTANCE_HISTORY_INSERT = "insert into flw_his_instance (id,tenant_id,process_id,instance_state,creator,create_time,end_time,parent_id,expire_time,instance_no,variable) values (?,?,?,?,?,?,?,?,?,?,?)";
    protected static final String INSTANCE_HISTORY_UPDATE = "update flw_his_instance set instance_state = ?, end_time = ?, variable = ? where id = ? ";
    protected static final String INSTANCE_HISTORY_DELETE = "delete from flw_his_instance where id = ?";
    protected static final String CCINSTANCE_INSERT = "insert into flw_cc_instance (instance_id, actor_id, creator, create_time, status) values (?, ?, ?, ?, ?)";
    protected static final String CCINSTANCE_UPDATE = "update flw_cc_instance set status = ?, finish_time = ? where instance_id = ? and actor_id = ?";
    protected static final String CCINSTANCE_DELETE = "delete from flw_cc_instance where instance_id = ? and actor_id = ?";
    protected static final String TASK_INSERT = "insert into flw_task (id,tenant_id,instance_id,task_name,display_name,task_type,perform_type,operator,create_time,finish_time,expire_time,action_url,parent_task_id,variable,version) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    protected static final String TASK_UPDATE = "update flw_task set finish_time=?, operator=?, variable=?, expire_time=?, action_url=?, version = version + 1 where id=? and version = ?";
    protected static final String TASK_DELETE = "delete from flw_task where id = ?";
    protected static final String TASK_HISTORY_INSERT = "insert into flw_his_task (id,instance_id,task_name,display_name,task_type,perform_type,task_state,operator,create_time,finish_time,expire_time,action_url,parent_task_id,variable) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    protected static final String TASK_HISTORY_DELETE = "delete from flw_his_task where id = ?";
    protected static final String TASK_ACTOR_INSERT = "insert into flw_task_actor (id,tenant_id,task_id, actor_id) values (?,?,?,?)";
    protected static final String TASK_ACTOR_DELETE = "delete from flw_task_actor where task_id = ?";
    protected static final String TASK_ACTOR_REDUCE = "delete from flw_task_actor where task_id = ? and actor_id = ?";
    protected static final String TASK_ACTOR_HISTORY_INSERT = "insert into flw_his_task_actor (task_id, actor_id) values (?, ?)";
    protected static final String TASK_ACTOR_HISTORY_DELETE = "delete from flw_his_task_actor where task_id = ?";
    protected static final String QUERY_VERSION = "select max(version) from flw_process ";
    protected static final String QUERY_PROCESS = "select id,name,display_name,type,instance_url,state, content, version,create_time,creator from flw_process ";
    protected static final String QUERY_INSTANCE = "select o.id,o.process_id,o.creator,o.create_time,o.parent_id,o.parent_node_name,o.expire_time,o.last_Update_time,o.last_updator,o.priority,o.instance_no,o.variable, o.version from flw_instance o ";
    protected static final String QUERY_task = "select id,instance_id,task_name,display_name,task_type,perform_type,operator,create_time,finish_time,expire_time,action_url,parent_task_id,variable, version from flw_task ";
    protected static final String QUERY_task_ACTOR = "select task_id, actor_id from flw_task_actor ";
    protected static final String QUERY_CCINSTANCE = "select instance_id, actor_id, creator, create_time, finish_time, status from flw_cc_instance ";
    protected static final String QUERY_HIST_INSTANCE = "select o.id,o.process_id,o.instance_state,o.priority,o.creator,o.create_time,o.end_time,o.parent_id,o.expire_time,o.instance_no,o.variable from flw_his_instance o ";
    protected static final String QUERY_HIST_task = "select id,instance_id,task_name,display_name,task_type,perform_type,task_state,operator,create_time,finish_time,expire_time,action_url,parent_task_id,variable from flw_his_task ";
    protected static final String QUERY_HIST_task_ACTOR = "select task_id, actor_id from flw_his_task_actor ";
    /**
     * 委托代理CRUD
     */
    protected static final String SURROGATE_INSERT = "insert into flw_surrogate (id, process_name, operator, surrogate, odate, sdate, edate, state) values (?,?,?,?,?,?,?,?)";
    protected static final String SURROGATE_UPDATE = "update flw_surrogate set process_name=?, surrogate=?, odate=?, sdate=?, edate=?, state=? where id = ?";
    protected static final String SURROGATE_DELETE = "delete from flw_surrogate where id = ?";
    protected static final String SURROGATE_QUERY = "select id, process_name, operator, surrogate, odate, sdate, edate, state from flw_surrogate";

    protected Dialect dialect;

    /**
     * 是否为ORM框架，用以标识对象直接持久化
     *
     * @return boolean
     */
    public abstract boolean isORM();

    /**
     * 保存或更新对象
     * isORM为true，则参数map只存放对象
     * isORM为false，则参数map需要放入SQL、ARGS、TYPE
     *
     * @param map 需要持久化的数据
     */
    public abstract void saveOrUpdate(Map<String, Object> map);

    @Override
    public void initialize(Object accessObject) {

    }

    /**
     * isORM为false，需要构造map传递给实现类
     *
     * @param sql  需要执行的sql语句
     * @param args sql语句中的参数列表
     * @param type sql语句中的参数类型
     * @return 构造的map
     */
    private Map<String, Object> buildMap(String sql, Object[] args, int[] type) {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_SQL, sql);
        map.put(KEY_ARGS, args);
        map.put(KEY_type, type);
        return map;
    }

    /**
     * isORM为true，只存放对象传递给orm框架
     *
     * @param entity 实体对象
     * @param su     保存或更新的标识
     * @return 构造的map
     */
    private Map<String, Object> buildMap(Object entity, String su) {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_ENTITY, entity);
        map.put(KEY_SU, su);
        return map;
    }

    /**
     * 获取数据库方言
     * 根据数据库连接的DatabaseMetaData获取数据库厂商，自动适配具体的方言
     * 当数据库类型未提供支持时无法自动获取方言，建议通过配置完成
     *
     * @return 方言对象
     */
    protected Dialect getDialect() {
        if (dialect != null) {
            return dialect;
        }
//        dialect = ServiceContext.getContext().find(Dialect.class);
        if (dialect == null) {
            try {
                dialect = JdbcHelper.getDialect(getConnection());
            } catch (Exception e) {
                log.error("Unable to find the available dialect.Please configure dialect to long.xml");
            }
        }
        return dialect;
    }

    /**
     * 由于process中涉及blob字段，未对各种框架统一，所以process操作交给具体的实现类处理
     */
    @Override
    public void saveProcess(Process process) {
        if (isORM()) {
            saveOrUpdate(buildMap(process, SAVE));
        } else {
            Object[] args = new Object[]{process.getId(), process.getTenantId(), process.getName(), process.getDisplayName(), process.getType(),
                    process.getInstanceUrl(), process.getState(), process.getVersion(), process.getCreateTime(), process.getCreator()};
            int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER,
                    Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(PROCESS_INSERT, args, type));
        }
    }

    /**
     * 由于process中涉及blob字段，未对各种框架统一，所以process操作交给具体的实现类处理
     */
    @Override
    public void updateProcess(Process process) {
        if (isORM()) {
            saveOrUpdate(buildMap(process, UPDATE));
        } else {
            Object[] args = new Object[]{process.getName(), process.getDisplayName(), process.getState(),
                    process.getInstanceUrl(), process.getCreateTime(), process.getCreator(), process.getId()};
            int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(PROCESS_UPDATE, args, type));
        }
    }

    @Override
    public void deleteProcess(Process process) {
        if (!isORM()) {
            Object[] args = new Object[]{process.getId()};
            int[] type = new int[]{Types.VARCHAR};
            saveOrUpdate(buildMap(PROCESS_DELETE, args, type));
        }
    }

    @Override
    public void updateProcessType(String id, String type) {
        if (isORM()) {
            Process process = getProcess(id);
            process.setType(type);
            saveOrUpdate(buildMap(process, UPDATE));
        } else {
            Object[] args = new Object[]{type, id};
            int[] types = new int[]{Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(PROCESS_UPDATE_type, args, types));
        }
    }

    @Override
    public void saveTask(Task task) {
        if (isORM()) {
            saveOrUpdate(buildMap(task, SAVE));
        } else {
            Object[] args = new Object[]{task.getId(), task.getTenantId(), task.getInstanceId(), task.getTaskName(), task.getDisplayName(), task.getTaskType(),
                    task.getPerformType(), task.getOperator(), task.getCreateTime(), task.getFinishTime(),
                    task.getExpireTime(), task.getActionUrl(), task.getParentTaskId(), task.getVariable(), task.getVersion()};
            int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER,
                    Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                    Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
            saveOrUpdate(buildMap(TASK_INSERT, args, type));
        }
    }

    @Override
    public void saveInstance(Instance instance) {
        if (isORM()) {
            saveOrUpdate(buildMap(instance, SAVE));
        } else {
            Object[] args = new Object[]{instance.getId(), instance.getTenantId(), instance.getProcessId(), instance.getCreator(), instance.getCreateTime(), instance.getParentId(),
                    instance.getParentNodeName(), instance.getExpireTime(), instance.getLastUpdateTime(), instance.getLastUpdator(), instance.getInstanceNo(),
                    instance.getVariable(), instance.getVersion()};
            int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                    Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
            saveOrUpdate(buildMap(INSTANCE_INSERT, args, type));
        }
    }

    @Override
    public void saveCCInstance(CCInstance ccinstance) {
        if (isORM()) {
            saveOrUpdate(buildMap(ccinstance, SAVE));
        } else {
            int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
            saveOrUpdate(buildMap(CCINSTANCE_INSERT, new Object[]{ccinstance.getInstanceId(), ccinstance.getActorId(),
                    ccinstance.getCreator(), ccinstance.getCreateTime(), ccinstance.getStatus()}, type));
        }
    }

    @Override
    public void saveTaskActor(TaskActor taskActor) {
        if (isORM()) {
            saveOrUpdate(buildMap(taskActor, SAVE));
        } else {
            int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(TASK_ACTOR_INSERT, new Object[]{taskActor.getId(), taskActor.getTenantId(), taskActor.getTaskId(), taskActor.getActorId()}, type));
        }
    }

    @Override
    public void updateTask(Task task) {
        if (isORM()) {
            saveOrUpdate(buildMap(task, UPDATE));
        } else {
            Object[] args = new Object[]{task.getFinishTime(), task.getOperator(), task.getVariable(), task.getExpireTime(), task.getActionUrl(), task.getId(), task.getVersion()};
            int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
            saveOrUpdate(buildMap(TASK_UPDATE, args, type));
        }
    }

    @Override
    public void updateInstance(Instance instance) {
        if (isORM()) {
            saveOrUpdate(buildMap(instance, UPDATE));
        } else {
            Object[] args = new Object[]{instance.getLastUpdator(), instance.getLastUpdateTime(), instance.getVariable(), instance.getExpireTime(), instance.getId(), instance.getVersion()};
            int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
            saveOrUpdate(buildMap(INSTANCE_UPDATE, args, type));
        }
    }

    @Override
    public void updateCCInstance(CCInstance ccinstance) {
        if (isORM()) {
            saveOrUpdate(buildMap(ccinstance, UPDATE));
        } else {
            Object[] args = new Object[]{ccinstance.getStatus(), ccinstance.getFinishTime(), ccinstance.getInstanceId(), ccinstance.getActorId()};
            int[] type = new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(CCINSTANCE_UPDATE, args, type));
        }
    }

    @Override
    public void deleteTask(Task task) {
        if (!isORM()) {
            Object[] args = new Object[]{task.getId()};
            int[] type = new int[]{Types.VARCHAR};
            saveOrUpdate(buildMap(TASK_ACTOR_DELETE, args, type));
            saveOrUpdate(buildMap(TASK_DELETE, args, type));
        }
    }

    @Override
    public void deleteInstance(Instance instance) {
        if (!isORM()) {
            int[] type = new int[]{Types.VARCHAR};
            saveOrUpdate(buildMap(INSTANCE_DELETE, new Object[]{instance.getId()}, type));
        }
    }

    @Override
    public void deleteCCInstance(CCInstance ccinstance) {
        if (!isORM()) {
            int[] type = new int[]{Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(CCINSTANCE_DELETE, new Object[]{ccinstance.getInstanceId(), ccinstance.getActorId()}, type));
        }
    }

    @Override
    public void removeTaskActor(String taskId, String... actors) {
        if (!isORM()) {
            for (String actorId : actors) {
                int[] type = new int[]{Types.VARCHAR, Types.VARCHAR};
                saveOrUpdate(buildMap(TASK_ACTOR_REDUCE, new Object[]{taskId, actorId}, type));
            }
        }
    }

    @Override
    public void saveHistory(HisInstance instance) {
        if (isORM()) {
            saveOrUpdate(buildMap(instance, SAVE));
        } else {
            Object[] args = new Object[]{instance.getId(), instance.getTenantId(), instance.getProcessId(), instance.getInstanceState(), instance.getCreator(),
                    instance.getCreateTime(), instance.getEndTime(), instance.getParentId(), instance.getExpireTime(), instance.getInstanceNo(), instance.getVariable()};
            int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR,
                    Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(INSTANCE_HISTORY_INSERT, args, type));
        }
    }

    @Override
    public void updateHistory(HisInstance instance) {
        if (isORM()) {
            saveOrUpdate(buildMap(instance, UPDATE));
        } else {
            Object[] args = new Object[]{instance.getInstanceState(), instance.getEndTime(), instance.getVariable(), instance.getId()};
            int[] type = new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(INSTANCE_HISTORY_UPDATE, args, type));
        }
    }

    @Override
    public void deleteHistoryInstance(HisInstance hisInstance) {
        if (!isORM()) {
            Object[] args = new Object[]{hisInstance.getId()};
            int[] type = new int[]{Types.VARCHAR};
            saveOrUpdate(buildMap(INSTANCE_HISTORY_DELETE, args, type));
        }
    }

    @Override
    public void saveHistory(HisTask task) {
        if (isORM()) {
            saveOrUpdate(buildMap(task, SAVE));
            if (task.getActorIds() != null) {
                for (String actorId : task.getActorIds()) {
                    if (StringUtils.isEmpty(actorId)) {
                        continue;
                    }
                    HisTaskActor hist = new HisTaskActor();
                    hist.setActorId(actorId);
                    hist.setTaskId(task.getId());
                    saveOrUpdate(buildMap(hist, SAVE));
                }
            }
        } else {
            Object[] args = new Object[]{task.getId(), task.getInstanceId(), task.getTaskName(), task.getDisplayName(), task.getTaskType(),
                    task.getPerformType(), task.getTaskState(), task.getOperator(), task.getCreateTime(), task.getFinishTime(),
                    task.getExpireTime(), task.getActionUrl(), task.getParentTaskId(), task.getVariable()};
            int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER,
                    Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(TASK_HISTORY_INSERT, args, type));
            if (task.getActorIds() != null) {
                for (String actorId : task.getActorIds()) {
                    if (StringUtils.isEmpty(actorId)) {
                        continue;
                    }
                    saveOrUpdate(buildMap(TASK_ACTOR_HISTORY_INSERT, new Object[]{task.getId(), actorId}, new int[]{Types.VARCHAR, Types.VARCHAR}));
                }
            }
        }
    }

    @Override
    public void deleteHistoryTask(HisTask hisTask) {
        if (!isORM()) {
            Object[] args = new Object[]{hisTask.getId()};
            int[] type = new int[]{Types.VARCHAR};
            saveOrUpdate(buildMap(TASK_ACTOR_HISTORY_DELETE, args, type));
            saveOrUpdate(buildMap(TASK_HISTORY_DELETE, args, type));
        }
    }

    @Override
    public void updateInstanceVariable(Instance instance) {
        updateInstance(instance);
        HisInstance hist = getHistInstance(instance.getId());
        hist.setVariable(instance.getVariable());
        updateHistory(hist);
    }

    @Override
    public void saveSurrogate(Surrogate surrogate) {
        if (isORM()) {
            saveOrUpdate(buildMap(surrogate, SAVE));
        } else {
            Object[] args = new Object[]{surrogate.getId(), surrogate.getProcessName(), surrogate.getOperator(),
                    surrogate.getSurrogate(), surrogate.getOdate(), surrogate.getSdate(), surrogate.getEdate(),
                    surrogate.getState()};
            int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                    Types.VARCHAR, Types.INTEGER};
            saveOrUpdate(buildMap(SURROGATE_INSERT, args, type));
        }
    }

    @Override
    public void updateSurrogate(Surrogate surrogate) {
        if (isORM()) {
            saveOrUpdate(buildMap(surrogate, UPDATE));
        } else {
            Object[] args = new Object[]{surrogate.getProcessName(), surrogate.getSurrogate(), surrogate.getOdate(),
                    surrogate.getSdate(), surrogate.getEdate(), surrogate.getState(), surrogate.getId()};
            int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                    Types.INTEGER, Types.VARCHAR};
            saveOrUpdate(buildMap(SURROGATE_UPDATE, args, type));
        }
    }

    @Override
    public void deleteSurrogate(Surrogate surrogate) {
        if (!isORM()) {
            Object[] args = new Object[]{surrogate.getId()};
            int[] type = new int[]{Types.VARCHAR};
            saveOrUpdate(buildMap(SURROGATE_DELETE, args, type));
        }
    }

    @Override
    public Surrogate getSurrogate(String id) {
        String where = " where id = ?";
        return queryObject(Surrogate.class, SURROGATE_QUERY + where, id);
    }

    @Override
    public List<Surrogate> getSurrogate(Page<Surrogate> page, QueryFilter filter) {
        StringBuilder sql = new StringBuilder(SURROGATE_QUERY);
        sql.append(" where 1=1 and state = 1 ");
        List<Object> paramList = new ArrayList<Object>();
        if (filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and process_name in(");
            for (int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (filter.getOperators() != null && filter.getOperators().length > 0) {
            sql.append(" and operator in (");
            for (String actor : filter.getOperators()) {
                sql.append("?,");
                paramList.add(actor);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (StringUtils.isNotEmpty(filter.getOperateTime())) {
            sql.append(" and sdate <= ? and edate >= ? ");
            paramList.add(filter.getOperateTime());
            paramList.add(filter.getOperateTime());
        }
        if (!filter.isInstanceOrderBy()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("sdate");
        }
        return queryList(page, filter, Surrogate.class, sql.toString(), paramList.toArray());
    }

    @Override
    public Task getTask(String taskId) {
        String where = " where id = ?";
        return queryObject(Task.class, QUERY_task + where, taskId);
    }

    @Override
    public List<Task> getNextActiveTasks(String parentTaskId) {
        String where = " where parent_task_id = ?";
        return queryList(Task.class, QUERY_task + where, parentTaskId);
    }

    @Override
    public List<Task> getNextActiveTasks(String instanceId, String taskName, String parentTaskId) {
        String sql = QUERY_task + " where parent_task_id in ( select ht.id from flw_his_task ht where ht.instance_id=? and ht.task_name=? and ht.parent_task_id=? )";
        return queryList(Task.class, sql, instanceId, taskName, parentTaskId);
    }

    @Override
    public HisTask getHistTask(String taskId) {
        String where = " where id = ?";
        return queryObject(HisTask.class, QUERY_HIST_task + where, taskId);
    }

    @Override
    public HisInstance getHistInstance(String instanceId) {
        String where = " where id = ?";
        return queryObject(HisInstance.class, QUERY_HIST_INSTANCE + where, instanceId);
    }

    @Override
    public List<TaskActor> getTaskActorsByTaskId(String taskId) {
        String where = " where task_id = ?";
        return queryList(TaskActor.class, QUERY_task_ACTOR + where, taskId);
    }

    @Override
    public List<HisTaskActor> getHistTaskActorsByTaskId(String taskId) {
        String where = " where task_id = ?";
        return queryList(HisTaskActor.class, QUERY_HIST_task_ACTOR + where, taskId);
    }

    @Override
    public Instance getInstance(String instanceId) {
        String where = " where id = ?";
        return queryObject(Instance.class, QUERY_INSTANCE + where, instanceId);
    }

    @Override
    public List<CCInstance> getCCInstance(String instanceId, String... actorIds) {
        StringBuilder where = new StringBuilder(QUERY_CCINSTANCE);
        where.append(" where 1 = 1 ");

        if (StringUtils.isNotEmpty(instanceId)) {
            where.append(" and instance_id = ?");
        }
        if (actorIds != null && actorIds.length > 0) {
            where.append(" and actor_id in (");
            where.append(org.apache.commons.lang.StringUtils.repeat("?,", actorIds.length));
            where.deleteCharAt(where.length() - 1);
            where.append(") ");
        }
        return queryList(CCInstance.class, where.toString(), ArrayUtils.add(actorIds, 0, instanceId));
    }

    @Override
    public Process getProcess(String id) {
        String where = " where id = ?";
        return queryObject(Process.class, QUERY_PROCESS + where, id);
    }

    @Override
    public List<Process> getProcess(Page<Process> page, QueryFilter filter) {
        StringBuilder sql = new StringBuilder(QUERY_PROCESS);
        sql.append(" where 1=1 ");
        List<Object> paramList = new ArrayList<Object>();
        if (filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and name in(");
            for (int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (filter.getVersion() != null) {
            sql.append(" and version = ? ");
            paramList.add(filter.getVersion());
        }
        if (filter.getState() != null) {
            sql.append(" and state = ? ");
            paramList.add(filter.getState());
        }
        if (StringUtils.isNotEmpty(filter.getDisplayName())) {
            sql.append(" and display_name like ? ");
            paramList.add("%" + filter.getDisplayName() + "%");
        }
        if (StringUtils.isNotEmpty(filter.getProcessType())) {
            sql.append(" and type = ? ");
            paramList.add(filter.getProcessType());
        }
        if (filter.getOperators() != null && filter.getOperators().length > 0) {
            sql.append(" and creator in(");
            for (int i = 0; i < filter.getOperators().length; i++) {
                sql.append("?,");
                paramList.add(filter.getOperators()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (!filter.isInstanceOrderBy()) {
            filter.setOrder(QueryFilter.ASC);
            filter.setOrderBy("name");
        }

        return queryList(page, filter, Process.class, sql.toString(), paramList.toArray());
    }

    @Override
    public List<Instance> getActiveInstances(Page<Instance> page, QueryFilter filter) {
        StringBuilder sql = new StringBuilder(QUERY_INSTANCE);
        sql.append(" left join flw_process p on p.id = o.process_id ");
        sql.append(" where 1=1 ");
        List<Object> paramList = new ArrayList<Object>();
        if (filter.getOperators() != null && filter.getOperators().length > 0) {
            sql.append(" and o.creator in(");
            for (int i = 0; i < filter.getOperators().length; i++) {
                sql.append("?,");
                paramList.add(filter.getOperators()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and p.name in(");
            for (int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (StringUtils.isNotEmpty(filter.getProcessId())) {
            sql.append(" and o.process_id = ? ");
            paramList.add(filter.getProcessId());
        }
        if (StringUtils.isNotEmpty(filter.getDisplayName())) {
            sql.append(" and p.display_name like ?");
            paramList.add("%" + filter.getDisplayName() + "%");
        }
        if (StringUtils.isNotEmpty(filter.getProcessType())) {
            sql.append(" and p.type = ? ");
            paramList.add(filter.getProcessType());
        }
        if (StringUtils.isNotEmpty(filter.getParentId())) {
            sql.append(" and o.parent_id = ? ");
            paramList.add(filter.getParentId());
        }
        if (filter.getExcludedIds() != null && filter.getExcludedIds().length > 0) {
            sql.append(" and o.id not in(");
            for (int i = 0; i < filter.getExcludedIds().length; i++) {
                sql.append("?,");
                paramList.add(filter.getExcludedIds()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (StringUtils.isNotEmpty(filter.getCreateTimeStart())) {
            sql.append(" and o.create_time >= ? ");
            paramList.add(filter.getCreateTimeStart());
        }
        if (StringUtils.isNotEmpty(filter.getCreateTimeEnd())) {
            sql.append(" and o.create_time <= ? ");
            paramList.add(filter.getCreateTimeEnd());
        }
        if (StringUtils.isNotEmpty(filter.getInstanceNo())) {
            sql.append(" and o.instance_no = ? ");
            paramList.add(filter.getInstanceNo());
        }

        if (!filter.isInstanceOrderBy()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("o.create_time");
        }
        return queryList(page, filter, Instance.class, sql.toString(), paramList.toArray());
    }

    @Override
    public List<Task> getActiveTasks(Page<Task> page, QueryFilter filter) {
        StringBuilder sql = new StringBuilder(QUERY_task);
        boolean isFetchActor = filter.getOperators() != null && filter.getOperators().length > 0;
        if (isFetchActor) {
            sql.append(" left join flw_task_actor ta on ta.task_id = id ");
        }
        sql.append(" where 1=1 ");
        List<Object> paramList = new ArrayList<Object>();
        if (StringUtils.isNotEmpty(filter.getInstanceId())) {
            sql.append(" and instance_id = ? ");
            paramList.add(filter.getInstanceId());
        }
        if (filter.getExcludedIds() != null && filter.getExcludedIds().length > 0) {
            sql.append(" and id not in(");
            for (int i = 0; i < filter.getExcludedIds().length; i++) {
                sql.append("?,");
                paramList.add(filter.getExcludedIds()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (isFetchActor) {
            sql.append(" and ta.actor_id in (");
            for (int i = 0; i < filter.getOperators().length; i++) {
                sql.append("?,");
                paramList.add(filter.getOperators()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and task_name in (");
            for (int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (StringUtils.isNotEmpty(filter.getCreateTimeStart())) {
            sql.append(" and create_time >= ? ");
            paramList.add(filter.getCreateTimeStart());
        }
        if (StringUtils.isNotEmpty(filter.getCreateTimeEnd())) {
            sql.append(" and create_time <= ? ");
            paramList.add(filter.getCreateTimeEnd());
        }
        if (!filter.isInstanceOrderBy()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("create_time");
        }
        return queryList(page, filter, Task.class, sql.toString(), paramList.toArray());
    }

    @Override
    public List<HisInstance> getHistoryInstances(Page<HisInstance> page, QueryFilter filter) {
        StringBuilder sql = new StringBuilder(QUERY_HIST_INSTANCE);
        sql.append(" left join flw_process p on p.id = o.process_id ");
        sql.append(" where 1=1 ");
        List<Object> paramList = new ArrayList<Object>();
        if (filter.getOperators() != null && filter.getOperators().length > 0) {
            sql.append(" and o.creator in(");
            for (int i = 0; i < filter.getOperators().length; i++) {
                sql.append("?,");
                paramList.add(filter.getOperators()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and p.name in(");
            for (int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (StringUtils.isNotEmpty(filter.getProcessId())) {
            sql.append(" and o.process_id = ? ");
            paramList.add(filter.getProcessId());
        }
        if (StringUtils.isNotEmpty(filter.getProcessType())) {
            sql.append(" and p.type = ? ");
            paramList.add(filter.getProcessType());
        }
        if (StringUtils.isNotEmpty(filter.getDisplayName())) {
            sql.append(" and p.display_name like ?");
            paramList.add("%" + filter.getDisplayName() + "%");
        }
        if (StringUtils.isNotEmpty(filter.getParentId())) {
            sql.append(" and o.parent_id = ? ");
            paramList.add(filter.getParentId());
        }
        if (filter.getState() != null) {
            sql.append(" and o.instance_state = ? ");
            paramList.add(filter.getState());
        }
        if (StringUtils.isNotEmpty(filter.getCreateTimeStart())) {
            sql.append(" and o.create_time >= ? ");
            paramList.add(filter.getCreateTimeStart());
        }
        if (StringUtils.isNotEmpty(filter.getCreateTimeEnd())) {
            sql.append(" and o.create_time <= ? ");
            paramList.add(filter.getCreateTimeEnd());
        }
        if (StringUtils.isNotEmpty(filter.getInstanceNo())) {
            sql.append(" and o.instance_no = ? ");
            paramList.add(filter.getInstanceNo());
        }
        if (!filter.isInstanceOrderBy()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("o.create_time");
        }
        return queryList(page, filter, HisInstance.class, sql.toString(), paramList.toArray());
    }

    @Override
    public List<HisTask> getHistoryTasks(Page<HisTask> page, QueryFilter filter) {
        StringBuilder sql = new StringBuilder(QUERY_HIST_task);
        boolean isFetchActor = filter.getOperators() != null && filter.getOperators().length > 0;
        if (isFetchActor) {
            sql.append(" left join flw_his_task_actor ta on ta.task_id = id ");
        }
        sql.append(" where 1=1 ");
        List<Object> paramList = new ArrayList<Object>();
        if (StringUtils.isNotEmpty(filter.getInstanceId())) {
            sql.append(" and instance_id = ? ");
            paramList.add(filter.getInstanceId());
        }
        if (isFetchActor) {
            sql.append(" and ta.actor_id in (");
            for (int i = 0; i < filter.getOperators().length; i++) {
                sql.append("?,");
                paramList.add(filter.getOperators()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and task_name in (");
            for (int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (StringUtils.isNotEmpty(filter.getCreateTimeStart())) {
            sql.append(" and create_time >= ? ");
            paramList.add(filter.getCreateTimeStart());
        }
        if (StringUtils.isNotEmpty(filter.getCreateTimeEnd())) {
            sql.append(" and create_time <= ? ");
            paramList.add(filter.getCreateTimeEnd());
        }
        if (!filter.isInstanceOrderBy()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("finish_time");
        }
        return queryList(page, filter, HisTask.class, sql.toString(), paramList.toArray());
    }

    @Override
    public List<WorkItem> getWorkItems(Page<WorkItem> page, QueryFilter filter) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select distinct o.process_id, t.instance_id, t.id as id, t.id as task_id, p.display_name as process_name, p.instance_url, o.parent_id, o.creator, ");
        sql.append(" o.create_time as instance_Create_time, o.expire_time as instance_Expire_time, o.instance_no, o.variable as instance_Variable, ");
        sql.append(" t.display_name as task_name, t.task_name as task_Key, t.task_type, t.perform_type, t.operator, t.action_url, ");
        sql.append(" t.create_time as task_Create_time, t.finish_time as task_End_time, t.expire_time as task_Expire_time, t.variable as task_Variable ");
        sql.append(" from flw_task t ");
        sql.append(" left join flw_instance o on t.instance_id = o.id ");
        sql.append(" left join flw_task_actor ta on ta.task_id=t.id ");
        sql.append(" left join flw_process p on p.id = o.process_id ");
        sql.append(" where 1=1 ");

        /**
         * 查询条件构造sql的where条件
         */
        List<Object> paramList = new ArrayList<Object>();
        if (filter.getOperators() != null && filter.getOperators().length > 0) {
            sql.append(" and ta.actor_id in (");
            for (String actor : filter.getOperators()) {
                sql.append("?,");
                paramList.add(actor);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }

        if (StringUtils.isNotEmpty(filter.getProcessId())) {
            sql.append(" and o.process_id = ?");
            paramList.add(filter.getProcessId());
        }
        if (StringUtils.isNotEmpty(filter.getDisplayName())) {
            sql.append(" and p.display_name like ?");
            paramList.add("%" + filter.getDisplayName() + "%");
        }
        if (StringUtils.isNotEmpty(filter.getParentId())) {
            sql.append(" and o.parent_id = ? ");
            paramList.add(filter.getParentId());
        }
        if (StringUtils.isNotEmpty(filter.getInstanceId())) {
            sql.append(" and t.instance_id = ? ");
            paramList.add(filter.getInstanceId());
        }
        if (filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and t.task_name in (");
            for (int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (filter.getTaskType() != null) {
            sql.append(" and t.task_type = ? ");
            paramList.add(filter.getTaskType());
        }
        if (filter.getPerformType() != null) {
            sql.append(" and t.perform_type = ? ");
            paramList.add(filter.getPerformType());
        }
        if (StringUtils.isNotEmpty(filter.getCreateTimeStart())) {
            sql.append(" and t.create_time >= ? ");
            paramList.add(filter.getCreateTimeStart());
        }
        if (StringUtils.isNotEmpty(filter.getCreateTimeEnd())) {
            sql.append(" and t.create_time <= ? ");
            paramList.add(filter.getCreateTimeEnd());
        }
        if (!filter.isInstanceOrderBy()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("t.create_time");
        }
        return queryList(page, filter, WorkItem.class, sql.toString(), paramList.toArray());
    }

    @Override
    public List<HisInstance> getCCWorks(Page<HisInstance> page, QueryFilter filter) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select id,process_id,instance_state,priority,cc.creator,cc.create_time,end_time,parent_id,expire_time,instance_no,variable ");
        sql.append(" from flw_cc_instance cc ");
        sql.append(" left join flw_his_instance o on cc.instance_id = o.id ");
        sql.append(" where 1=1 ");

        /**
         * 查询条件构造sql的where条件
         */
        List<Object> paramList = new ArrayList<Object>();
        if (filter.getOperators() != null && filter.getOperators().length > 0) {
            sql.append(" and cc.actor_id in(");
            for (int i = 0; i < filter.getOperators().length; i++) {
                sql.append("?,");
                paramList.add(filter.getOperators()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (filter.getState() != null) {
            sql.append(" and cc.status = ? ");
            paramList.add(filter.getState());
        }
        if (StringUtils.isNotEmpty(filter.getProcessId())) {
            sql.append(" and process_id = ? ");
            paramList.add(filter.getProcessId());
        }
        if (StringUtils.isNotEmpty(filter.getParentId())) {
            sql.append(" and parent_id = ? ");
            paramList.add(filter.getParentId());
        }
        if (StringUtils.isNotEmpty(filter.getCreateTimeStart())) {
            sql.append(" and cc.create_time >= ? ");
            paramList.add(filter.getCreateTimeStart());
        }
        if (StringUtils.isNotEmpty(filter.getCreateTimeEnd())) {
            sql.append(" and cc.create_time <= ? ");
            paramList.add(filter.getCreateTimeEnd());
        }
        if (StringUtils.isNotEmpty(filter.getInstanceNo())) {
            sql.append(" and instance_no = ? ");
            paramList.add(filter.getInstanceNo());
        }
        if (!filter.isInstanceOrderBy()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("cc.create_time");
        }
        return queryList(page, filter, HisInstance.class, sql.toString(), paramList.toArray());
    }

    @Override
    public List<WorkItem> getHistoryWorkItems(Page<WorkItem> page, QueryFilter filter) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select distinct o.process_id, t.instance_id, t.id as id, t.id as task_id, p.display_name as process_name, p.instance_url, o.parent_id, o.creator, ");
        sql.append(" o.create_time as instance_Create_time, o.expire_time as instance_Expire_time, o.instance_no, o.variable as instance_Variable, ");
        sql.append(" t.display_name as task_name, t.task_name as task_Key, t.task_type, t.perform_type,t.operator, t.action_url, ");
        sql.append(" t.create_time as task_Create_time, t.finish_time as task_End_time, t.expire_time as task_Expire_time, t.variable as task_Variable ");
        sql.append(" from flw_his_task t ");
        sql.append(" left join flw_his_instance o on t.instance_id = o.id ");
        sql.append(" left join flw_his_task_actor ta on ta.task_id=t.id ");
        sql.append(" left join flw_process p on p.id = o.process_id ");
        sql.append(" where 1=1 ");
        /**
         * 查询条件构造sql的where条件
         */
        List<Object> paramList = new ArrayList<Object>();
        if (filter.getOperators() != null && filter.getOperators().length > 0) {
            sql.append(" and ta.actor_id in (");
            for (String actor : filter.getOperators()) {
                sql.append("?,");
                paramList.add(actor);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }

        if (StringUtils.isNotEmpty(filter.getProcessId())) {
            sql.append(" and o.process_id = ?");
            paramList.add(filter.getProcessId());
        }
        if (StringUtils.isNotEmpty(filter.getDisplayName())) {
            sql.append(" and p.display_name like ?");
            paramList.add("%" + filter.getDisplayName() + "%");
        }
        if (StringUtils.isNotEmpty(filter.getParentId())) {
            sql.append(" and o.parent_id = ? ");
            paramList.add(filter.getParentId());
        }
        if (StringUtils.isNotEmpty(filter.getInstanceId())) {
            sql.append(" and t.instance_id = ? ");
            paramList.add(filter.getInstanceId());
        }
        if (filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and t.task_name in (");
            for (int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (filter.getTaskType() != null) {
            sql.append(" and t.task_type = ? ");
            paramList.add(filter.getTaskType());
        }
        if (filter.getPerformType() != null) {
            sql.append(" and t.perform_type = ? ");
            paramList.add(filter.getPerformType());
        }
        if (StringUtils.isNotEmpty(filter.getCreateTimeStart())) {
            sql.append(" and t.create_time >= ? ");
            paramList.add(filter.getCreateTimeStart());
        }
        if (StringUtils.isNotEmpty(filter.getCreateTimeEnd())) {
            sql.append(" and t.create_time <= ? ");
            paramList.add(filter.getCreateTimeEnd());
        }
        if (!filter.isInstanceOrderBy()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("t.create_time");
        }
        return queryList(page, filter, WorkItem.class, sql.toString(), paramList.toArray());
    }

    @Override
    public <T> List<T> queryList(Page<T> page, QueryFilter filter, Class<T> clazz, String sql, Object... args) {
        String orderBy = StringUtils.buildPageOrder(filter.getOrder(), filter.getOrderBy());
        String querySQL = sql + orderBy;
        if (page == null) {
            return queryList(clazz, querySQL, args);
        }
        String countSQL = "select count(1) from (" + sql + ") c ";
        querySQL = getDialect().getPageSql(querySQL, page);
        if (log.isDebugEnabled()) {
            log.debug("查询分页countSQL=\n" + countSQL);
            log.debug("查询分页querySQL=\n" + querySQL);
        }
        try {
            Object count = queryCount(countSQL, args);
            List<T> list = queryList(clazz, querySQL, args);
            if (list == null) {
                list = Collections.emptyList();
            }
            page.setResult(list);
            page.setTotalCount(ClassUtils.castLong(count));
            return list;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 运行脚本
     */
    @Override
    public void runScript() {
        String autoStr = ConfigHelper.getProperty("schema.auto");
        if (autoStr == null || !autoStr.equalsIgnoreCase("true")) {
            return;
        }
        Connection conn = null;
        try {
            conn = getConnection();
            if (JdbcHelper.isExec(conn)) {
                log.info("script has completed execution.skip this step");
                return;
            }
            String databaseType = JdbcHelper.getDatabaseType(conn);
            String schema = "db/core/schema-" + databaseType + ".sql";
            ScriptRunner runner = new ScriptRunner(conn, true);
            runner.runScript(schema);
        } catch (Exception e) {
            throw new FlowLongException(e);
        } finally {
            try {
                JdbcHelper.close(conn);
            } catch (SQLException e) {
                //ignore
            }
        }
    }

    /**
     * 分页查询时，符合条件的总记录数
     *
     * @param sql  sql语句
     * @param args 参数数组
     * @return 总记录数
     */
    protected abstract Object queryCount(String sql, Object... args);

    /**
     * 获取数据库连接
     *
     * @return Connection
     */
    protected abstract Connection getConnection() throws SQLException;
}
