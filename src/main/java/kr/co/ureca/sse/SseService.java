package kr.co.ureca.sse;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
@Getter
public class SseService{
    /** SSE 콜백은 SSE 와는 다른 스레드에서 실행된다. 따라서 thread-safe 한 자료구조를 사용해야
     * ConcurrentModificationException 을 방지할 수 있다.
     */
    private final List<SseEmitter> emitterList = new CopyOnWriteArrayList<>();
    private static final long TIMEOUT = 60 * 1000;
    private static final long RECONNECTION_TIMEOUT = 1000L;

    public SseEmitter createEmitter() {
        SseEmitter sseEmitter = new SseEmitter(TIMEOUT);
        emitterList.add(sseEmitter);

        //클라이언트 연결 종료 시
        sseEmitter.onCompletion(() -> emitterList.remove(sseEmitter));
        sseEmitter.onTimeout(() -> emitterList.remove(sseEmitter));
        return sseEmitter;
    }

    public void broadcastSeatStatusBySse(String seatStatusJson){
        for(SseEmitter emitter : emitterList){
            try{
                emitter.send(SseEmitter.event()
                                .name("Sse로 예약 상태 브로드캐스팅")
                                .reconnectTime(RECONNECTION_TIMEOUT)
                                .data(seatStatusJson, MediaType.APPLICATION_JSON));
            }catch (IOException e){
                emitterList.remove(emitter);
                log.error("Sse 브로드캐스팅 실패 {}",e.getMessage());
            }
        }
    }
}
