package com.flowlong.bpm.engine.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @author he.wenyao
 */
public interface CommonMapper<T> extends BaseMapper<T> {

    /**
     * 批量插入
     * @param entityList
     * @return
     */
    int insertBatchSomeColumn(List<T> entityList);
}
