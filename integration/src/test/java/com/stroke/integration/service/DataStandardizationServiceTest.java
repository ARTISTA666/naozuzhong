package com.stroke.integration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DataStandardizationService — 数据标准化")
class DataStandardizationServiceTest {

    private DataStandardizationService service;

    @BeforeEach
    void setUp() {
        service = new DataStandardizationService();
    }

    @Test
    @DisplayName("性别标准化: 男/M/1 → M")
    void genderMale() {
        assertEquals("M", service.standardizeGender("男"));
        assertEquals("M", service.standardizeGender("M"));
        assertEquals("M", service.standardizeGender("1"));
        assertEquals("M", service.standardizeGender("male"));
    }

    @Test
    @DisplayName("性别标准化: 女/F/2 → F")
    void genderFemale() {
        assertEquals("F", service.standardizeGender("女"));
        assertEquals("F", service.standardizeGender("F"));
        assertEquals("F", service.standardizeGender("2"));
    }

    @Test
    @DisplayName("性别标准化: 未知/空 → U/null")
    void genderUnknown() {
        assertEquals("U", service.standardizeGender("未知"));
        assertNull(service.standardizeGender(""));
        assertNull(service.standardizeGender(null));
    }

    @Test
    @DisplayName("日期解析: yyyy-MM-dd HH:mm:ss")
    void parseStandard() {
        LocalDateTime dt = service.parseDateTime("2026-05-19 08:30:00");
        assertNotNull(dt);
        assertEquals(2026, dt.getYear());
        assertEquals(5, dt.getMonthValue());
        assertEquals(19, dt.getDayOfMonth());
    }

    @Test
    @DisplayName("日期解析: yyyyMMddHHmmss")
    void parseCompact() {
        LocalDateTime dt = service.parseDateTime("20260519083000");
        assertNotNull(dt);
        assertEquals(2026, dt.getYear());
        assertEquals(5, dt.getMonthValue());
    }

    @Test
    @DisplayName("日期解析: 非法字符串返回null")
    void parseInvalid() {
        assertNull(service.parseDateTime("not-a-date"));
        assertNull(service.parseDateTime(""));
        assertNull(service.parseDateTime(null));
    }

    @Test
    @DisplayName("时间序列校验: 发病时间晚于到院时间 → 返回警告")
    void onsetAfterArrival() {
        LocalDateTime onset = LocalDateTime.of(2026, 5, 19, 10, 0);
        LocalDateTime arrival = LocalDateTime.of(2026, 5, 19, 8, 0);
        String warning = service.validateTimeSequence(onset, arrival);
        assertNotNull(warning);
        assertTrue(warning.contains("发病时间晚于到院时间"));
    }

    @Test
    @DisplayName("时间序列校验: 正常顺序 → 返回null")
    void onsetBeforeArrival() {
        LocalDateTime onset = LocalDateTime.of(2026, 5, 19, 8, 0);
        LocalDateTime arrival = LocalDateTime.of(2026, 5, 19, 10, 0);
        assertNull(service.validateTimeSequence(onset, arrival));
    }

    @Test
    @DisplayName("LOINC映射: 已知编码")
    void mapToLoinc() {
        assertEquals("2345-7", service.mapToLoinc("GLU", "血糖"));
        assertEquals("777-3", service.mapToLoinc("PLT", "血小板"));
    }

    @Test
    @DisplayName("LOINC映射: 未知编码返回null")
    void mapToLoincUnknown() {
        assertNull(service.mapToLoinc("UNKNOWN", "未知项目"));
        assertNull(service.mapToLoinc(null, null));
    }
}
