package com.stroke.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * integration 服务启动类
 * <p>
 * 职责：对接HIS/LIS/PACS/区域平台，标准化外部数据，提供FHIR网关
 */
@SpringBootApplication(scanBasePackages = "com.stroke")
public class IntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationApplication.class, args);
    }
}
