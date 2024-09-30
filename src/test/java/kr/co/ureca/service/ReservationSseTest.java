package kr.co.ureca.service;

import kr.co.ureca.dto.SseEventPayload;
import kr.co.ureca.sse.SseService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SseServiceTest {

    @InjectMocks
    private SseService sseHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Mockito 초기화
    }

    @Test
    void testCreateEmitter() {
        SseEmitter emitter = sseHandler.createEmitter();

        // SSEEmitter가 정상적으로 생성되었는지 확인
        assertEquals(1, sseHandler.getEmitterList().size());
    }

    @Test
    void testSendSeatStatusBySse() throws IOException {
        SseEmitter emitter = mock(SseEmitter.class);  // Mock SseEmitter 객체
        sseHandler.getEmitterList().add(emitter);

        String seatStatusJson = "{\"seatNo\": " + 1L+ ", \"status\": " + true + "}";
        sseHandler.broadcastSeatStatusBySse(seatStatusJson);

        // 클라이언트에게 데이터가 정상적으로 전송되었는지 확인
        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void testEmitterOnCompletion() throws InterruptedException {
        // Emitter 생성
        SseEmitter emitter = sseHandler.createEmitter();

        // Emitter 목록에 추가되었는지 확인
        assertEquals(1, sseHandler.getEmitterList().size());

        final boolean[] isCompleted = {false};

        // 콜백 등록 확인
        System.out.println("Registering onCompletion callback...");
        emitter.onCompletion(() -> {
            System.out.println("onCompletion callback executed!");
            isCompleted[0] = true;
        });

        // Emitter 완료
        System.out.println("Calling emitter.complete()...");
        emitter.complete();

        // Awaitility로 비동기 대기 (타임아웃을 5초로 늘림)
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> isCompleted[0]);

        // Emitter가 완료되면 목록에서 제거되는지 확인
        assertEquals(true, isCompleted[0], "The onCompletion callback should have been executed.");
        assertEquals(0, sseHandler.getEmitterList().size(), "The emitter should have been removed from the list.");
    }


}
