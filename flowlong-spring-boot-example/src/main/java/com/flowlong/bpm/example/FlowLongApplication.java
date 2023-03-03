package com.flowlong.bpm.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FlowLongApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(FlowLongApplication.class, args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
