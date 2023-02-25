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
package com.flowlong.bpm.engine.core;

import com.flowlong.bpm.engine.DBAccess;

/**
 * 作为抽象父类，提供给子类access实现方式
 *
 * @author hubin
 * @since 1.0
 */
public abstract class AccessService {
    /**
     * 状态；活动状态
     */
    protected Integer STATE_ACTIVE = 1;
    /**
     * 状态：结束状态
     */
    protected Integer STATE_FINISH = 0;
    /**
     * 状态：终止状态
     */
    protected Integer STATE_TERMINATION = 2;
    /**
     * 数据库的access
     */
    protected DBAccess dbAccess;

    /**
     * 获取DBAccess，供子类使用
     */
    public DBAccess access() {
        return dbAccess;
    }

    /**
     * setter
     *
     * @param dbAccess 访问对象
     */
    public void setAccess(DBAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

}
