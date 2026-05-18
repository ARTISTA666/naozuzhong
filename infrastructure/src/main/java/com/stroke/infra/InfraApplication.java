package com.stroke.infra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * infrastructure 服务启动类
 * <p>
 * 职责：用户与权限、审计、通知推送、CDS规则引擎
 */
@SpringBootApplication(scanBasePackages = "com.stroke")
public class InfraApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfraApplication.class, args);
    }
}
