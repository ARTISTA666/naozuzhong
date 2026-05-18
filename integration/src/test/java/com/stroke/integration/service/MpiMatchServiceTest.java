package com.stroke.integration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MpiMatchService — 患者主索引匹配")
class MpiMatchServiceTest {

    private MpiMatchService service;

    @BeforeEach
    void setUp() {
        service = new MpiMatchService();
    }

    @Test
    @DisplayName("身份证号完全匹配 → 成功")
    void matchByIdCard() {
        MpiMatchService.MatchResult result = service.matchPatient(
                "110101199001011234", "张三",
                LocalDate.of(1990, 1, 1), "M");
        assertTrue(result.isMatched());
        assertEquals("ID_CARD", result.getMatchRule());
        assertEquals(1.0, result.getConfidence());
        assertEquals(1L, result.getPatientId());
    }

    @Test
    @DisplayName("姓名+出生日期匹配 → 成功")
    void matchByNameAndDob() {
        MpiMatchService.MatchResult result = service.matchPatient(
                null, "张三",
                LocalDate.of(1990, 1, 1), "M");
        assertTrue(result.isMatched());
        assertEquals("NAME_DOB", result.getMatchRule());
    }

    @Test
    @DisplayName("无匹配 → isMatched=false")
    void noMatch() {
        MpiMatchService.MatchResult result = service.matchPatient(
                "unknown-id", "李四",
                LocalDate.of(2000, 1, 1), "M");
        assertFalse(result.isMatched());
        assertEquals(0.0, result.getConfidence());
    }

    @Test
    @DisplayName("姓名+出生日期匹配但性别不同 → 仍需匹配但置信度略低")
    void matchByNameDobDifferentGender() {
        MpiMatchService.MatchResult result = service.matchPatient(
                null, "张三",
                LocalDate.of(1990, 1, 1), "F");
        assertTrue(result.isMatched());
        assertTrue(result.getConfidence() >= 0.9);
    }
}
