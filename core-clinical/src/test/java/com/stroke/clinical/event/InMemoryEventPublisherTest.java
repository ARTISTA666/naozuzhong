package com.stroke.clinical.event;

import com.stroke.domain.event.BaseEvent;
import com.stroke.domain.event.CtTimeoutWarningEvent;
import com.stroke.domain.event.DntTimeoutWarningEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 内存事件发布器单元测试
 */
@DisplayName("InMemoryEventPublisher — 事件发布/订阅")
class InMemoryEventPublisherTest {

    private InMemoryEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new InMemoryEventPublisher();
        publisher.init();
    }

    @Test
    @DisplayName("发布事件 — 监听器收到事件")
    void publishEventTriggerListener() {
        AtomicInteger counter = new AtomicInteger(0);
        publisher.registerListener("TEST_EVENT", event -> counter.incrementAndGet());

        publisher.publish(new TestEvent("TEST_EVENT", 1L, 1L));

        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("发布事件 — 不同类型不触发")
    void publishEventNoMatch() {
        AtomicInteger counter = new AtomicInteger(0);
        publisher.registerListener("TYPE_A", event -> counter.incrementAndGet());

        publisher.publish(new TestEvent("TYPE_B", 1L, 1L));

        assertEquals(0, counter.get());
    }

    @Test
    @DisplayName("多个监听器 — 全部触发")
    void multipleListeners() {
        AtomicInteger counter = new AtomicInteger(0);
        publisher.registerListener("TEST", event -> counter.incrementAndGet());
        publisher.registerListener("TEST", event -> counter.incrementAndGet());

        publisher.publish(new TestEvent("TEST", 1L, 1L));

        assertEquals(2, counter.get());
    }

    @Test
    @DisplayName("全局监听器 — 收到所有事件")
    void globalListener() {
        List<String> receivedTypes = new ArrayList<>();
        publisher.registerGlobalListener(event -> receivedTypes.add(event.getEventType()));

        publisher.publish(new TestEvent("EVENT_A", 1L, 1L));
        publisher.publish(new TestEvent("EVENT_B", 2L, 2L));

        assertEquals(2, receivedTypes.size());
        assertTrue(receivedTypes.contains("EVENT_A"));
        assertTrue(receivedTypes.contains("EVENT_B"));
    }

    @Test
    @DisplayName("DNT超时事件 — 正确设置字段")
    void dntTimeoutEvent() {
        DntTimeoutWarningEvent event = new DntTimeoutWarningEvent(
                "event-001", 100L, 1000L, 52L, 45L);
        event.setCurrentStatus("THROMBOLYSIS_IN_PROGRESS");

        assertEquals("DNT_TIMEOUT_WARNING", event.getEventType());
        assertEquals(100L, event.getEncounterId());
        assertEquals(1000L, event.getPatientId());
        assertEquals(52L, event.getDntMinutes());
        assertEquals(45L, event.getThreshold());
        assertEquals("THROMBOLYSIS_IN_PROGRESS", event.getCurrentStatus());
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("CT超时事件 — 正确设置字段")
    void ctTimeoutEvent() {
        CtTimeoutWarningEvent event = new CtTimeoutWarningEvent(
                "event-002", 200L, 2000L, 30L, 25L);

        assertEquals("CT_TIMEOUT_WARNING", event.getEventType());
        assertEquals(200L, event.getEncounterId());
        assertEquals(30L, event.getCtMinutes());
    }

    @Test
    @DisplayName("监听器异常 — 不影响其他监听器")
    void listenerExceptionDoesNotAffectOthers() {
        AtomicInteger counter = new AtomicInteger(0);

        publisher.registerListener("TEST", event -> { throw new RuntimeException("模拟异常"); });
        publisher.registerListener("TEST", event -> counter.incrementAndGet());

        publisher.publish(new TestEvent("TEST", 1L, 1L));

        assertEquals(1, counter.get(), "第二个监听器应正常执行");
    }

    // ==================== 辅助测试事件类 ====================

    static class TestEvent extends BaseEvent {
        TestEvent(String eventType, Long encounterId, Long patientId) {
            super("test-" + System.nanoTime(), eventType, encounterId, patientId);
        }
    }
}
