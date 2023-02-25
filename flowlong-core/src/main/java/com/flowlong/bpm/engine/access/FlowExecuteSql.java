package com.flowlong.bpm.engine.access;

public interface FlowExecuteSql {
    String PROCESS_INSERT = "insert into flw_process (id,name,display_Name,type,instance_Url,state,version,create_Time,creator) values (?,?,?,?,?,?,?,?,?)";
    String PROCESS_UPDATE = "update flw_process set name=?, display_Name=?,state=?,instance_Url=?,create_Time=?,creator=? where id=? ";
    String PROCESS_DELETE = "delete from flw_process where id = ?";
    String PROCESS_UPDATE_BLOB = "update flw_process set content=? where id=?";
    String PROCESS_UPDATE_TYPE = "update flw_process set type=? where id=?";

    String ORDER_INSERT = "insert into flw_order (id,process_Id,creator,create_Time,parent_Id,parent_Node_Name,expire_Time,last_Update_Time,last_Updator,order_No,variable,version) values (?,?,?,?,?,?,?,?,?,?,?,?)";
    String ORDER_UPDATE = "update flw_order set last_Updator=?, last_Update_Time=?, variable = ?, expire_Time=?, version = version + 1 where id=? and version = ?";
    String ORDER_DELETE = "delete from flw_order where id = ?";
    String ORDER_HISTORY_INSERT = "insert into flw_hist_order (id,process_Id,order_State,creator,create_Time,end_Time,parent_Id,expire_Time,order_No,variable) values (?,?,?,?,?,?,?,?,?,?)";
    String ORDER_HISTORY_UPDATE = "update flw_hist_order set order_State = ?, end_Time = ?, variable = ? where id = ? ";
    String ORDER_HISTORY_DELETE = "delete from flw_hist_order where id = ?";

    String CCORDER_INSERT = "insert into flw_cc_order (order_Id, actor_Id, creator, create_Time, status) values (?, ?, ?, ?, ?)";
    String CCORDER_UPDATE = "update flw_cc_order set status = ?, finish_Time = ? where order_Id = ? and actor_Id = ?";
    String CCORDER_DELETE = "delete from flw_cc_order where order_Id = ? and actor_Id = ?";

    String TASK_INSERT = "insert into flw_task (id,order_Id,task_Name,display_Name,task_Type,perform_Type,operator,create_Time,finish_Time,expire_Time,action_Url,parent_Task_Id,variable,version) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    String TASK_UPDATE = "update flw_task set finish_Time=?, operator=?, variable=?, expire_Time=?, action_Url=?, version = version + 1 where id=? and version = ?";
    String TASK_DELETE = "delete from flw_task where id = ?";
    String TASK_HISTORY_INSERT = "insert into flw_hist_task (id,order_Id,task_Name,display_Name,task_Type,perform_Type,task_State,operator,create_Time,finish_Time,expire_Time,action_Url,parent_Task_Id,variable) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    String TASK_HISTORY_DELETE = "delete from flw_hist_task where id = ?";

    String TASK_ACTOR_INSERT = "insert into flw_task_actor (task_Id, actor_Id) values (?, ?)";
    String TASK_ACTOR_DELETE = "delete from flw_task_actor where task_Id = ?";
    String TASK_ACTOR_REDUCE = "delete from flw_task_actor where task_Id = ? and actor_Id = ?";
    String TASK_ACTOR_HISTORY_INSERT = "insert into flw_hist_task_actor (task_Id, actor_Id) values (?, ?)";
    String TASK_ACTOR_HISTORY_DELETE = "delete from flw_hist_task_actor where task_Id = ?";

    String QUERY_VERSION = "select max(version) from flw_process ";
    String QUERY_PROCESS = "select id,name,display_Name,type,instance_Url,state, content, version,create_Time,creator from flw_process ";
    String QUERY_ORDER = "select o.id,o.process_Id,o.creator,o.create_Time,o.parent_Id,o.parent_Node_Name,o.expire_Time,o.last_Update_Time,o.last_Updator,o.priority,o.order_No,o.variable, o.version from flw_order o ";
    String QUERY_TASK = "select id,order_Id,task_Name,display_Name,task_Type,perform_Type,operator,create_Time,finish_Time,expire_Time,action_Url,parent_Task_Id,variable, version from flw_task ";
    String QUERY_TASK_ACTOR = "select task_Id, actor_Id from flw_task_actor ";
    String QUERY_CCORDER = "select order_Id, actor_Id, creator, create_Time, finish_Time, status from flw_cc_order ";

    String QUERY_HIST_ORDER = "select o.id,o.process_Id,o.order_State,o.priority,o.creator,o.create_Time,o.end_Time,o.parent_Id,o.expire_Time,o.order_No,o.variable from flw_hist_order o ";
    String QUERY_HIST_TASK = "select id,order_Id,task_Name,display_Name,task_Type,perform_Type,task_State,operator,create_Time,finish_Time,expire_Time,action_Url,parent_Task_Id,variable from flw_hist_task ";
    String QUERY_HIST_TASK_ACTOR = "select task_Id, actor_Id from flw_hist_task_actor ";

    /**
     * 委托代理CRUD
     */
    String SURROGATE_INSERT = "insert into flw_surrogate (id, process_Name, operator, surrogate, odate, sdate, edate, state) values (?,?,?,?,?,?,?,?)";
    String SURROGATE_UPDATE = "update flw_surrogate set process_Name=?, surrogate=?, odate=?, sdate=?, edate=?, state=? where id = ?";
    String SURROGATE_DELETE = "delete from flw_surrogate where id = ?";
    String SURROGATE_QUERY = "select id, process_Name, operator, surrogate, odate, sdate, edate, state from flw_surrogate";

}
