package com.stroke.clinical.service;

import com.stroke.clinical.dto.StateChangeRequest;
import com.stroke.clinical.event.InMemoryEventPublisher;
import com.stroke.clinical.repository.mapper.GreenwayHistoryMapper;
import com.stroke.clinical.repository.mapper.GreenwayMapper;
import com.stroke.clinical.service.greenway.GreenwayStateMachine;
import com.stroke.clinical.service.greenway.StateTransitionRegistry;
import com.stroke.common.BusinessException;
import com.stroke.domain.entity.StrokeGreenway;
import com.stroke.domain.entity.StrokeGreenwayHistory;
import com.stroke.domain.enums.GreenwayStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GreenwayService 超时事件发布测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GreenwayService — 超时事件发布")
class GreenwayServiceTimeoutTest {

    @Mock private GreenwayMapper greenwayMapper;
    @Mock private GreenwayHistoryMapper historyMapper;

    private InMemoryEventPublisher eventPublisher;
    private GreenwayStateMachine stateMachine;
    private GreenwayService service;

    @Captor
    private ArgumentCaptor<StrokeGreenway> greenwayCaptor;

    @BeforeEach
    void setUp() {
        StateTransitionRegistry registry = new StateTransitionRegistry();
        registry.init();
        stateMachine = new GreenwayStateMachine(registry);
        eventPublisher = new InMemoryEventPublisher();
        eventPublisher.init();
        service = new GreenwayService(greenwayMapper, historyMapper, stateMachine, eventPublisher);
    }

    @Test
    @DisplayName("DNT阈值内不提权 — 不触发事件")
    void normalDntNoEvent() {
        StrokeGreenway g = createGreenway(100L);
        g.setNeedleTime(g.getDoorTime().plusMinutes(30)); // DNT = 30min < 45

        when(greenwayMapper.findByEncounterId(100L)).thenReturn(g);
        when(greenwayMapper.optimisticUpdate(any())).thenReturn(1);

        AtomicInteger eventCount = new AtomicInteger(0);
        eventPublisher.registerListener("DNT_TIMEOUT_WARNING", e -> eventCount.incrementAndGet());

        service.changeState(100L, request("THROMBOLYSIS_COMPLETED"));

        assertEquals(0, eventCount.get(), "DNT未超时不应发布事件");
    }

    @Test
    @DisplayName("DNT超45分钟 — 触发 DNT_TIMEOUT_WARNING 事件")
    void dntExceededTriggersEvent() {
        StrokeGreenway g = createGreenway(100L);
        g.setNeedleTime(g.getDoorTime().plusMinutes(60)); // DNT = 60min > 45

        when(greenwayMapper.findByEncounterId(100L)).thenReturn(g);
        when(greenwayMapper.optimisticUpdate(any())).thenReturn(1);

        AtomicInteger dntEventCount = new AtomicInteger(0);
        eventPublisher.registerListener("DNT_TIMEOUT_WARNING", e -> dntEventCount.incrementAndGet());

        service.changeState(100L, request("THROMBOLYSIS_COMPLETED"));

        assertEquals(1, dntEventCount.get(), "DNT超时应发布1个事件");
    }

    @Test
    @DisplayName("CT超25分钟 — 触发 CT_TIMEOUT_WARNING 事件")
    void ctExceededTriggersEvent() {
        StrokeGreenway g = createGreenway(100L);
        g.setCtCompleteTime(g.getDoorTime().plusMinutes(35)); // CT = 35min > 25

        when(greenwayMapper.findByEncounterId(100L)).thenReturn(g);
        when(greenwayMapper.optimisticUpdate(any())).thenReturn(1);

        AtomicInteger ctEventCount = new AtomicInteger(0);
        eventPublisher.registerListener("CT_TIMEOUT_WARNING", e -> ctEventCount.incrementAndGet());

        service.changeState(100L, request("AWAITING_DECISION"));

        assertEquals(1, ctEventCount.get(), "CT超时应发布1个事件");
    }

    private StrokeGreenway createGreenway(Long encounterId) {
        StrokeGreenway g = new StrokeGreenway();
        g.setId(1L);
        g.setEncounterId(encounterId);
        g.setPatientId(1000L);
        g.setStatus(GreenwayStatus.THROMBOLYSIS_IN_PROGRESS.name());
        g.setDoorTime(LocalDateTime.now().minusHours(1));
        g.setCtOrderTime(LocalDateTime.now().minusMinutes(50));
        g.setCtCompleteTime(LocalDateTime.now().minusMinutes(25));
        g.setVersion(0);
        return g;
    }

    private StateChangeRequest request(String targetStatus) {
        StateChangeRequest r = new StateChangeRequest();
        r.setTargetStatus(targetStatus);
        r.setOperatorId("test-doctor");
        return r;
    }
}
