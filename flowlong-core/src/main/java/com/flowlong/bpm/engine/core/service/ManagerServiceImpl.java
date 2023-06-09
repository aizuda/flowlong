/* Copyright 2023-2025 jobob@qq.com
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
package com.flowlong.bpm.engine.core.service;

import com.flowlong.bpm.engine.ManagerService;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.core.enums.InstanceState;
import com.flowlong.bpm.engine.core.mapper.SurrogateMapper;
import com.flowlong.bpm.engine.entity.Surrogate;
import org.springframework.stereotype.Service;

/**
 * 管理服务类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Service
public class ManagerServiceImpl implements ManagerService {
    private SurrogateMapper surrogateMapper;

    public ManagerServiceImpl(SurrogateMapper surrogateMapper) {
        this.surrogateMapper = surrogateMapper;
    }

    @Override
    public void saveOrUpdate(Surrogate surrogate) {
        Assert.notNull(surrogate);
        surrogate.setState(InstanceState.active);
        if (null == surrogate.getId()) {
            surrogateMapper.insert(surrogate);
        } else {
            surrogateMapper.updateById(surrogate);
        }
    }

    @Override
    public void deleteSurrogate(String id) {
        Surrogate surrogate = getSurrogate(id);
        Assert.notNull(surrogate);
        surrogateMapper.deleteById(id);
    }

    @Override
    public Surrogate getSurrogate(String id) {
        return surrogateMapper.selectById(id);
    }

    @Override
    public String getSurrogate(String createBy, String processName) {
        // 待处理
        return "";
    }
}
