package com.shangluhua.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class ShangLuHuaApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShangLuHuaApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        System.out.println("======================================");
        System.out.println("  商陆花 启动成功!");
        System.out.println("  管理后台: http://localhost:8080");
        System.out.println("  健康检查: http://localhost:8080/api/health");
        System.out.println("======================================");
    }
}
