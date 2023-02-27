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
package com.flowlong.bpm.engine.model;

import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.assist.ClassUtils;
import com.flowlong.bpm.engine.assist.ReflectUtils;
import com.flowlong.bpm.engine.assist.StringUtils;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.handler.FlowLongHandler;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 自定义模型
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class CustomModel extends WorkModel {

    /**
     * 需要执行的class类路径
     */
    private String clazz;
    /**
     * 需要执行的class对象的方法名称
     */
    private String methodName;
    /**
     * 执行方法时传递的参数表达式变量名称
     */
    private String args;
    /**
     * 执行的返回值变量
     */
    private String var;
    /**
     * 加载模型时初始化的对象实例
     */
    private Object invokeObject;

    @Override
    protected void run(FlowLongContext flowLongContext, Execution execution) {
        if (invokeObject == null) {
            invokeObject = ClassUtils.newInstance(clazz);
        }
        if (invokeObject == null) {
            throw new FlowLongException("自定义模型[class=" + clazz + "]实例化对象失败");
        }

        if (invokeObject instanceof FlowLongHandler) {
            FlowLongHandler handler = (FlowLongHandler) invokeObject;
            handler.handle(flowLongContext, execution);
        } else {
            Method method = ReflectUtils.findMethod(invokeObject.getClass(), methodName);
            if (method == null) {
                throw new FlowLongException("自定义模型[class=" + clazz + "]无法找到方法名称:" + methodName);
            }
            Object[] objects = getArgs(execution.getArgs(), args);
            Object returnValue = ReflectUtils.invoke(method, invokeObject, objects);
            if (StringUtils.isNotEmpty(var)) {
                execution.getArgs().put(var, returnValue);
            }
        }
        execution.getEngine().taskService().history(execution, this);
        runOutTransition(flowLongContext, execution);
    }

    /**
     * 根据传递的执行参数、模型的参数列表返回实际的参数对象数组
     *
     * @param execArgs 运行时传递的参数数据
     * @param args     自定义节点需要的参数
     * @return 调用自定义节点类方法的参数数组
     */
    private Object[] getArgs(Map<String, Object> execArgs, String args) {
        Object[] objects = null;
        if (StringUtils.isNotEmpty(args)) {
            String[] argArray = args.split(",");
            objects = new Object[argArray.length];
            for (int i = 0; i < argArray.length; i++) {
                objects[i] = execArgs.get(argArray[i]);
            }
        }
        return objects;
    }
}
