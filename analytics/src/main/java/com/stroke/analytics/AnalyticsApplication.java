package com.stroke.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * analytics 服务启动类
 * <p>
 * 职责：BI报表、质控指标计算、科研数据导出。
 * 从业务库通过CDC抽取数据到ODS，聚合为DWD/DWS。
 * 严格禁止在OLTP库执行复杂分析查询。
 */
@SpringBootApplication(scanBasePackages = "com.stroke")
public class AnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsApplication.class, args);
    }
}
