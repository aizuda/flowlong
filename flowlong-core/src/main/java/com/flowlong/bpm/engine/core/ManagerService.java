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
import com.flowlong.bpm.engine.IManagerService;
import com.flowlong.bpm.engine.access.Page;
import com.flowlong.bpm.engine.access.QueryFilter;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.StringUtils;
import com.flowlong.bpm.engine.entity.Surrogate;

import java.util.List;

/**
 * 管理服务类
 *
 * @author hubin
 * @since 1.0
 */
public class ManagerService extends AccessService implements IManagerService {

    public ManagerService(DBAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

    @Override
    public void saveOrUpdate(Surrogate surrogate) {
        Assert.notNull(surrogate);
        surrogate.setState(STATE_ACTIVE);
        if (StringUtils.isEmpty(surrogate.getId())) {
            surrogate.setId(StringUtils.getPrimaryKey());
            access().saveSurrogate(surrogate);
        } else {
            access().updateSurrogate(surrogate);
        }
    }

    @Override
    public void deleteSurrogate(String id) {
        Surrogate surrogate = getSurrogate(id);
        Assert.notNull(surrogate);
        access().deleteSurrogate(surrogate);
    }

    @Override
    public Surrogate getSurrogate(String id) {
        return access().getSurrogate(id);
    }

    @Override
    public List<Surrogate> getSurrogate(QueryFilter filter) {
        Assert.notNull(filter);
        return access().getSurrogate(null, filter);
    }

    @Override
    public List<Surrogate> getSurrogate(Page<Surrogate> page, QueryFilter filter) {
        Assert.notNull(filter);
        return access().getSurrogate(page, filter);
    }

    @Override
    public String getSurrogate(String operator, String processName) {
        Assert.notEmpty(operator);
        QueryFilter filter = new QueryFilter().
                setOperator(operator).
                setOperateTime(DateUtils.getTime());
        if (StringUtils.isNotEmpty(processName)) {
            filter.setName(processName);
        }
        List<Surrogate> surrogates = getSurrogate(filter);
        if (surrogates == null || surrogates.isEmpty()) {
            return operator;
        }
        StringBuffer buffer = new StringBuffer(50);
        for (Surrogate surrogate : surrogates) {
            String result = getSurrogate(surrogate.getSurrogate(), processName);
            buffer.append(result).append(",");
        }
        buffer.deleteCharAt(buffer.length() - 1);
        return buffer.toString();
    }
}
