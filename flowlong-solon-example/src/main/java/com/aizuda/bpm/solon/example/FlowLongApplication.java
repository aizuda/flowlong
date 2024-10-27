package com.aizuda.bpm.solon.example;

import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;
import org.noear.solon.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SolonMain
public class FlowLongApplication {

    /**
     * 运行该模块注释根目录 build.gradle 文件 afterEvaluate 代码块
     */
    public static void main(String[] args) {
        Solon.start(FlowLongApplication.class, args, app -> {
        });
    }
}
