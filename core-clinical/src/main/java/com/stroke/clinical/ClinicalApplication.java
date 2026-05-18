package com.stroke.clinical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * core-clinical 服务启动类
 * <p>
 * 职责：患者主索引、就诊、急诊绿道状态机、所有量表评定（NIHSS/mRS等）、溶栓/取栓记录
 */
@SpringBootApplication(scanBasePackages = "com.stroke")
public class ClinicalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicalApplication.class, args);
    }
}
