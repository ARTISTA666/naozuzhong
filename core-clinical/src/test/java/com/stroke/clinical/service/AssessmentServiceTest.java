package com.stroke.clinical.service;

import com.stroke.clinical.dto.assessment.NihssAssessmentVO;
import com.stroke.clinical.dto.assessment.SubmitNihssRequest;
import com.stroke.clinical.repository.mapper.AssessmentItemMapper;
import com.stroke.clinical.repository.mapper.AssessmentMapper;
import com.stroke.clinical.service.assessment.NihssScaleConfig;
import com.stroke.common.BusinessException;
import com.stroke.domain.entity.Assessment;
import com.stroke.domain.entity.AssessmentItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 评估服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AssessmentService — NIHSS评估服务")
class AssessmentServiceTest {

    @Mock private AssessmentMapper assessmentMapper;
    @Mock private AssessmentItemMapper assessmentItemMapper;

    private NihssScaleConfig nihssConfig;
    private AssessmentService service;

    @Captor
    private ArgumentCaptor<Assessment> assessmentCaptor;

    @BeforeEach
    void setUp() {
        nihssConfig = new NihssScaleConfig();
        nihssConfig.init();
        service = new AssessmentService(assessmentMapper, assessmentItemMapper, nihssConfig);
    }

    @Test
    @DisplayName("提交 NIHSS — 正常提交成功")
    void submitNihss() {
        SubmitNihssRequest req = createValidRequest();

        when(assessmentMapper.getMaxVersion(100L)).thenReturn(0);
        when(assessmentMapper.findLatestNihss(100L)).thenReturn(null);
        when(assessmentMapper.insert(any())).thenReturn(1);
        when(assessmentItemMapper.insert(any())).thenReturn(1);

        NihssAssessmentVO result = service.submitNihss(req);

        assertNotNull(result);
        assertEquals(100L, result.getEncounterId());
        assertEquals(1, result.getVersionNo());
        assertTrue(result.getTotalScore() > 0);
        assertEquals("ONLINE", result.getSource());
        assertEquals("active", result.getRecordStatus());
    }

    @Test
    @DisplayName("提交 NIHSS — 第二次提交版本号递增")
    void submitNihssVersionIncrement() {
        SubmitNihssRequest req = createValidRequest();

        // 模拟已有版本1
        Assessment existing = new Assessment();
        existing.setId(1L);
        existing.setRecordStatus("active");
        existing.setVersionNo(1);
        when(assessmentMapper.getMaxVersion(100L)).thenReturn(1);
        when(assessmentMapper.findLatestNihss(100L)).thenReturn(existing);
        when(assessmentMapper.insert(any())).thenReturn(1);
        when(assessmentItemMapper.insert(any())).thenReturn(1);

        NihssAssessmentVO result = service.submitNihss(req);

        assertEquals(2, result.getVersionNo(), "版本应递增为2");

        // 验证旧版本被标记为superseded
        verify(assessmentMapper).updateById(argThat(a -> a.getRecordStatus().equals("superseded")));
    }

    @Test
    @DisplayName("提交 NIHSS — 离线标记")
    void submitNihssOffline() {
        SubmitNihssRequest req = createValidRequest();
        req.setSource("OFFLINE");

        when(assessmentMapper.getMaxVersion(100L)).thenReturn(0);
        when(assessmentMapper.findLatestNihss(100L)).thenReturn(null);
        when(assessmentMapper.insert(any())).thenReturn(1);
        when(assessmentItemMapper.insert(any())).thenReturn(1);

        NihssAssessmentVO result = service.submitNihss(req);

        assertEquals("OFFLINE", result.getSource());
    }

    @Test
    @DisplayName("提交 NIHSS — 非法分数抛出异常")
    void submitInvalidScores() {
        SubmitNihssRequest req = createValidRequest();
        req.setScores(Map.of("1a", 99));  // 非法分数

        assertThrows(IllegalArgumentException.class,
                () -> service.submitNihss(req));
    }

    @Test
    @DisplayName("提交 NIHSS — 不存在的条目编码抛出异常")
    void submitUnknownItemCode() {
        SubmitNihssRequest req = createValidRequest();
        req.setScores(Map.of("unknown", 0));

        assertThrows(IllegalArgumentException.class,
                () -> service.submitNihss(req));
    }

    @Test
    @DisplayName("查询最新 NIHSS — 存在记录返回正确")
    void getLatestNihssFound() {
        Assessment assessment = new Assessment();
        assessment.setId(1L);
        assessment.setEncounterId(100L);
        assessment.setTotalScore(6);
        assessment.setVersionNo(2);
        assessment.setSource("ONLINE");
        assessment.setRecordStatus("active");

        when(assessmentMapper.findLatestNihss(100L)).thenReturn(assessment);
        when(assessmentItemMapper.findByAssessmentId(1L)).thenReturn(List.of());

        NihssAssessmentVO result = service.getLatestNihss(100L);

        assertEquals(2, result.getVersionNo());
        assertEquals(6, result.getTotalScore());
    }

    @Test
    @DisplayName("查询最新 NIHSS — 不存在抛出404")
    void getLatestNihssNotFound() {
        when(assessmentMapper.findLatestNihss(100L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> service.getLatestNihss(100L));
    }

    @Test
    @DisplayName("查询量表配置 — 返回15项")
    void getScaleItems() {
        var items = service.getScaleItems();
        assertEquals(15, items.size());
        assertEquals("1a", items.get(0).getItemCode());
        assertEquals(0, items.get(0).getMinScore());
        assertEquals(3, items.get(0).getMaxScore());
    }

    private SubmitNihssRequest createValidRequest() {
        SubmitNihssRequest req = new SubmitNihssRequest();
        req.setEncounterId(100L);
        req.setPatientId(1000L);
        req.setScores(Map.ofEntries(
                Map.entry("1a", 1), Map.entry("1b", 0), Map.entry("1c", 0),
                Map.entry("2", 0), Map.entry("3", 0), Map.entry("4", 2),
                Map.entry("5a", 3), Map.entry("5b", 0), Map.entry("6a", 0),
                Map.entry("6b", 0), Map.entry("7", 0), Map.entry("8", 1),
                Map.entry("9", 0), Map.entry("10", 0), Map.entry("11", 0)));
        req.setSource("ONLINE");
        req.setOperatorId("doc-001");
        req.setOperatorName("张医生");
        return req;
    }
}
