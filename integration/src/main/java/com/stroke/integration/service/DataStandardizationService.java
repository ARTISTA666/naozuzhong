package com.stroke.integration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据标准化服务 — 对应设计文档 3.5 节 & 8.1 节
 * <p>
 * 职责：
 * 1. 字段标准化：性别、时间格式统一
 * 2. 异常检测：发病时间晚于到院时间等
 * 3. 缺失修复：身份证号缺失时用姓名+出生日期做MPI候选
 */
@Service
public class DataStandardizationService {

    private static final Logger log = LoggerFactory.getLogger(DataStandardizationService.class);

    /** 性别映射：各系统传入值 → 统一代码 */
    private static final Map<String, String> GENDER_MAP = new ConcurrentHashMap<>();

    /** 常见日期时间格式 */
    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    public DataStandardizationService() {
        // 性别映射初始化
        GENDER_MAP.put("M", "M");
        GENDER_MAP.put("男", "M");
        GENDER_MAP.put("male", "M");
        GENDER_MAP.put("1", "M");
        GENDER_MAP.put("F", "F");
        GENDER_MAP.put("女", "F");
        GENDER_MAP.put("female", "F");
        GENDER_MAP.put("2", "F");
        GENDER_MAP.put("U", "U");
        GENDER_MAP.put("未知", "U");
        GENDER_MAP.put("unknown", "U");
    }

    /**
     * 标准化性别代码
     *
     * @param rawGender 原始性别值
     * @return 标准化代码: M/F/U，无法识别返回 null
     */
    public String standardizeGender(String rawGender) {
        if (rawGender == null || rawGender.isBlank()) return null;
        String standardized = GENDER_MAP.get(rawGender.trim());
        if (standardized == null) {
            log.warn("无法识别的性别值: {}", rawGender);
            return null;
        }
        return standardized;
    }

    /**
     * 解析日期时间字符串（容错多种格式）
     *
     * @param dateStr 日期时间字符串
     * @return 解析后的 LocalDateTime，解析失败返回 null
     */
    public LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;

        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDateTime.parse(dateStr.trim(), fmt);
            } catch (DateTimeParseException ignored) {
                // 尝试下一种格式
            }
        }

        // 尝试解析为日期（仅日期的情况）
        try {
            LocalDate date = LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return date.atStartOfDay();
        } catch (DateTimeParseException ignored) {
        }

        log.warn("无法解析的日期时间: {}", dateStr);
        return null;
    }

    /**
     * 检测时间逻辑异常
     * <p>
     * 对应设计文档 8.1 节：发病时间晚于到院时间 → 标记可疑
     *
     * @param onsetTime 发病时间
     * @param arrivalTime 到院时间
     * @return 异常描述，无异常返回 null
     */
    public String validateTimeSequence(LocalDateTime onsetTime, LocalDateTime arrivalTime) {
        if (onsetTime != null && arrivalTime != null && onsetTime.isAfter(arrivalTime)) {
            String msg = String.format("发病时间(%s)晚于到院时间(%s)，数据可疑",
                    onsetTime, arrivalTime);
            log.warn("时间序列异常: {}", msg);
            return msg;
        }
        return null;
    }

    /**
     * 标准化院内的检验项目编码到 LOINC
     * <p>
     * 对应设计文档 3.5 节：院内项目码映射到LOINC
     *
     * @param localCode  院内项目编码
     * @param localName  院内项目名称
     * @return 映射后的 LOINC 编码，无映射返回 null
     */
    public String mapToLoinc(String localCode, String localName) {
        // TODO: 接入真实 LOINC 映射表
        // 当前返回模拟映射
        if (localCode == null) return null;
        Map<String, String> mockMapping = Map.of(
                "GLU", "2345-7",   // Glucose
                "INR", "6301-6",   // INR
                "PLT", "777-3",    // Platelet count
                "WBC", "6690-2",   // WBC
                "CR", "2160-0"     // Creatinine
        );
        return mockMapping.get(localCode.toUpperCase());
    }
}
