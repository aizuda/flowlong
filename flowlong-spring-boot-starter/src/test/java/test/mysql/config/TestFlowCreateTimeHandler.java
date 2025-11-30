package test.mysql.config;

import com.aizuda.bpm.engine.FlowDataTransfer;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.core.enums.ExecuteType;
import com.aizuda.bpm.engine.handler.FlowCreateTimeHandler;
import test.mysql.TestCreateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestFlowCreateTimeHandler implements FlowCreateTimeHandler {
    private final Map<Long, String> instanceIdMap = new ConcurrentHashMap<>();

    @Override
    public Date getCurrentTime(ExecuteType executeType, Long instanceId, Long taskId) {
        String setTime = null;
        if (ExecuteType.process != executeType) {
            // 创建实例后续逻辑
            if (null != instanceId) {
                setTime = instanceIdMap.get(instanceId);
            }
            if (null == setTime) {
                setTime = FlowDataTransfer.get(TestCreateTime.setFlowCreateTime);
                if (null != setTime && null != instanceId) {
                    // 缓存实例指定时间
                    instanceIdMap.put(instanceId, setTime);
                    // 移除传递参数
                    FlowDataTransfer.removeByKey(TestCreateTime.setFlowCreateTime);
                }
            }
        }
        return getSetTime(setTime);
    }

    @Override
    public Date getFinishTime(Long instanceId, Long taskId) {
        if (null == instanceId) {
            return DateUtils.getCurrentDate();
        }
        String setTime = instanceIdMap.get(instanceId);
        if (null != setTime && null == taskId) {
            // 实例完成，删除缓存记录
            instanceIdMap.remove(instanceId);
        }
        return getSetTime(setTime);
    }

    public Date getSetTime(String setTime) {
        if (null != setTime) {
            try {
                // 获取当前时间的时分秒
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                String currentTimeStr = timeFormat.format(new Date());

                // 组合日期和时间
                String combinedDateTimeStr = setTime + " " + currentTimeStr;
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return dateTimeFormat.parse(combinedDateTimeStr);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
        return DateUtils.getCurrentDate();
    }
}
