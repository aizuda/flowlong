package com.flowlong.bpm.solon.example;

import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;
import org.noear.solon.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SolonMain
public class FlowLongApp {
    public static void main(String[] args) {
        Solon.start(FlowLongApp.class, args);
    }
}
