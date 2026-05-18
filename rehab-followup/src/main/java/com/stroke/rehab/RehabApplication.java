package com.stroke.rehab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * rehab-followup 服务启动类
 * <p>
 * 职责：康复评定与计划、随访任务、二级预防、患者端数据接收。
 * 院后业务低实时性，独立部署升级不影响绿道。
 */
@SpringBootApplication(scanBasePackages = "com.stroke")
public class RehabApplication {

    public static void main(String[] args) {
        SpringApplication.run(RehabApplication.class, args);
    }
}
