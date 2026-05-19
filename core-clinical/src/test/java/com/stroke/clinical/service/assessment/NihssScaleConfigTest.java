package com.stroke.clinical.service.assessment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NIHSS 量表配置单元测试
 */
@DisplayName("NihssScaleConfig — 量表配置与校验")
class NihssScaleConfigTest {

    private NihssScaleConfig config;

    @BeforeEach
    void setUp() {
        config = new NihssScaleConfig();
        config.init();
    }

    @Test
    @DisplayName("初始化为15项")
    void itemCount() {
        assertEquals(15, config.getAllItems().size());
    }

    @Test
    @DisplayName("所有条目编码正确")
    void itemCodes() {
        List<String> codes = config.getAllItems().stream()
                .map(NihssScaleConfig.NihssItemDef::getCode)
                .collect(Collectors.toList());
        assertIterableEquals(
                java.util.List.of("1a", "1b", "1c", "2", "3", "4", "5a", "5b",
                        "6a", "6b", "7", "8", "9", "10", "11"),
                codes);
    }

    @Test
    @DisplayName("分数验证 — 合法分数通过")
    void validScores() {
        assertTrue(config.isValidScore("1a", 0));
        assertTrue(config.isValidScore("1a", 3));
        assertTrue(config.isValidScore("5a", 4));
        assertTrue(config.isValidScore("11", 2));
    }

    @Test
    @DisplayName("分数验证 — 非法分数拒绝")
    void invalidScores() {
        assertFalse(config.isValidScore("1a", -1));
        assertFalse(config.isValidScore("1a", 4));
        assertFalse(config.isValidScore("5a", 5));
        assertFalse(config.isValidScore("unknown", 0));
    }

    @Test
    @DisplayName("计算总分为0")
    void calculateTotalZero() {
        Map<String, Integer> scores = Map.of(
                "1a", 0, "1b", 0, "1c", 0, "2", 0, "3", 0,
                "4", 0, "5a", 0, "5b", 0, "6a", 0, "6b", 0,
                "7", 0, "8", 0, "9", 0, "10", 0, "11", 0);
        assertEquals(0, config.validateAndCalculate(scores));
    }

    @Test
    @DisplayName("计算总分正常")
    void calculateTotalNormal() {
        Map<String, Integer> scores = Map.of(
                "1a", 1, "4", 2, "5a", 3, "9", 1, "11", 1);
        assertEquals(8, config.validateAndCalculate(scores));
    }

    @Test
    @DisplayName("非法分数抛出异常")
    void invalidScoreThrows() {
        Map<String, Integer> scores = Map.of("1a", 5);  // 最大3分
        assertThrows(IllegalArgumentException.class,
                () -> config.validateAndCalculate(scores));
    }

    @Test
    @DisplayName("严重程度分级")
    void severityLevel() {
        assertEquals("正常", config.getSeverityLevel(0));
        assertEquals("轻度", config.getSeverityLevel(4));
        assertEquals("中度", config.getSeverityLevel(15));
        assertEquals("中重度", config.getSeverityLevel(20));
        assertEquals("重度", config.getSeverityLevel(42));
    }
}
