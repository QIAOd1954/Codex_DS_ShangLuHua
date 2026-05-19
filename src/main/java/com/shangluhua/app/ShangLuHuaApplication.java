package com.shangluhua.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableCaching
public class ShangLuHuaApplication {

    private static final Logger log = LoggerFactory.getLogger(ShangLuHuaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ShangLuHuaApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("======================================");
        log.info("  商陆花 启动成功!");
        log.info("  管理后台: http://localhost:8080");
        log.info("  健康检查: http://localhost:8080/api/health");
        log.info("======================================");
    }
}
