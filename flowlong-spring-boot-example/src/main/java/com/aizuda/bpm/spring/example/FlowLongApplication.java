package com.aizuda.bpm.spring.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FlowLongApplication {

    /**
     * 运行该模块注释根目录 build.gradle 文件 afterEvaluate 代码块
     */
    public static void main(String[] args) {
        try {
            SpringApplication.run(FlowLongApplication.class, args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
