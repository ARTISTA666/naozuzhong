package com.stroke.clinical.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stroke.clinical.dto.CreateGreenwayRequest;
import com.stroke.clinical.dto.GreenwayVO;
import com.stroke.clinical.dto.StateChangeRequest;
import com.stroke.clinical.service.GreenwayService;
import com.stroke.common.Result;
import com.stroke.domain.entity.StrokeGreenway;
import com.stroke.domain.entity.StrokeGreenwayHistory;
import com.stroke.domain.enums.GreenwayStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 绿道控制器单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GreenwayController — 绿道REST API")
class GreenwayControllerTest {

    @Mock
    private GreenwayService greenwayService;

    private GreenwayController controller;

    @BeforeEach
    void setUp() {
        controller = new GreenwayController(greenwayService);
    }

    @Test
    @DisplayName("POST /greenway — 创建绿道成功")
    void createGreenway() {
        // 准备
        CreateGreenwayRequest req = new CreateGreenwayRequest();
        req.setPatientId(100L);
        req.setEncounterId(200L);
        req.setDoorTime(LocalDateTime.now());

        StrokeGreenway saved = new StrokeGreenway();
        saved.setId(1L);
        saved.setEncounterId(200L);
        saved.setStatus(GreenwayStatus.WAITING_TRIAGE.name());

        GreenwayVO mockVo = new GreenwayVO();
        mockVo.setId(1L);
        mockVo.setEncounterId(200L);
        mockVo.setStatus("WAITING_TRIAGE");

        when(greenwayService.createGreenway(anyLong(), anyLong(), any(), any()))
                .thenReturn(saved);
        when(greenwayService.getGreenwayDetail(200L))
                .thenReturn(mockVo);

        // 执行
        Result<GreenwayVO> result = controller.createGreenway(req);

        // 验证
        assertTrue(result.getCode() == 200);
        assertNotNull(result.getData());
        assertEquals("WAITING_TRIAGE", result.getData().getStatus());
        verify(greenwayService).createGreenway(100L, 200L, null, req.getDoorTime());
    }

    @Test
    @DisplayName("POST /greenway/{id}/state-change — 状态变更成功")
    void changeState() {
        StateChangeRequest req = new StateChangeRequest();
        req.setTargetStatus("CT_ORDERED");
        req.setOperatorId("doctor-1");

        StrokeGreenway updated = new StrokeGreenway();
        updated.setId(1L);
        updated.setEncounterId(200L);
        updated.setStatus("CT_ORDERED");

        GreenwayVO mockVo = new GreenwayVO();
        mockVo.setId(1L);
        mockVo.setStatus("CT_ORDERED");

        when(greenwayService.changeState(eq(200L), any())).thenReturn(updated);
        when(greenwayService.getGreenwayDetail(200L)).thenReturn(mockVo);

        Result<GreenwayVO> result = controller.changeState(200L, req);

        assertTrue(result.getCode() == 200);
        assertEquals("CT_ORDERED", result.getData().getStatus());
    }

    @Test
    @DisplayName("GET /greenway/{id} — 获取绿道详情")
    void getDetail() {
        GreenwayVO mockVo = new GreenwayVO();
        mockVo.setId(1L);
        mockVo.setEncounterId(200L);
        mockVo.setStatus("CT_COMPLETED");

        when(greenwayService.getGreenwayDetail(200L)).thenReturn(mockVo);

        Result<GreenwayVO> result = controller.getGreenwayDetail(200L);

        assertEquals(200, result.getCode());
        assertEquals("CT_COMPLETED", result.getData().getStatus());
    }

    @Test
    @DisplayName("GET /greenway/{id}/history — 获取历史记录")
    void getHistory() {
        StrokeGreenwayHistory h = new StrokeGreenwayHistory();
        h.setGreenwayId(1L);
        h.setFromStatus("TRIAGING");
        h.setToStatus("CT_ORDERED");

        when(greenwayService.getHistory(200L)).thenReturn(List.of(h));

        Result<List<StrokeGreenwayHistory>> result = controller.getHistory(200L);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("CT_ORDERED", result.getData().get(0).getToStatus());
    }
}
