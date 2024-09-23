package kr.co.ureca.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ReservationWebSocketHandler extends TextWebSocketHandler {
    /**
     * 여러 클라이언트와의 연결을 유지하기 위해 CopyOnWriteArraySet 을 사용하여 연결된 세션을 관리
     */
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session); // 클라이언트 연결 시 세션 저장
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session); // 연결 종료 시 세션 제거
    }

    /**
     * 좌석 상태 변경 시 모든 클라이언트에게 브로드캐스트
     */
    public void broadcastSeatStatus(String seatStatus) throws IOException {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(seatStatus));
            }
        }
    }
}
