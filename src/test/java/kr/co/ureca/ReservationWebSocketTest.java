package kr.co.ureca;

import kr.co.ureca.dto.SseEventPayload;
import kr.co.ureca.websocket.WebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.Mockito.*;

public class ReservationWebSocketTest {

    @InjectMocks
    private WebSocketHandler webSocketHandler;

    @Mock
    private WebSocketSession session;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConnection() throws Exception {
        webSocketHandler.afterConnectionEstablished(session);
        verify(session, never()).close();
    }


    @Test
    void testBroadcastSeatStatus() throws Exception {
        // WebSocket으로 클라이언트에게 브로드캐스트 메시지 전송
        String seatStatusJson = "{\"seatNo\": " + 1L+ ", \"status\": " + true + "}";


        when(session.isOpen()).thenReturn(true);

        webSocketHandler.afterConnectionEstablished(session);
        webSocketHandler.broadcastSeatStatus(seatStatusJson);

        // 클라이언트에게 메시지가 정상적으로 전송됐는지 검증
        verify(session).sendMessage(new TextMessage(seatStatusJson));
    }


    @Test
    void testAfterConnectionClosed() throws Exception {
        // 연결이 종료됐을 때 처리
        webSocketHandler.afterConnectionEstablished(session);
        webSocketHandler.afterConnectionClosed(session, null);

        // 세션이 연결 리스트에서 제거됐는지 확인
        verify(session, never()).sendMessage(any(TextMessage.class));  // 메시지가 전송되지 않았음을 확인
    }
}
