package com.flowlong.bpm.engine.scheduling.impl;

import com.flowlong.bpm.engine.scheduling.FlowLongScheduler;
import com.flowlong.bpm.engine.scheduling.JobEntity;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
public class DefaultScheduler implements FlowLongScheduler {

    @Override
    public void schedule(JobEntity jobEntity) {

    }

    @Override
    public void delete(String key) {

    }
}
