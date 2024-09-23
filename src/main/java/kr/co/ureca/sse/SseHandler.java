package kr.co.ureca.sse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseHandler{
    private final List<SseEmitter> emitterList = new CopyOnWriteArrayList<>();

    public SseEmitter createEmitter() {
        SseEmitter sseEmitter = new SseEmitter();
        emitterList.add(sseEmitter);

        //클라이언트 연결 종료 시
        sseEmitter.onCompletion(() -> emitterList.remove(sseEmitter));
        sseEmitter.onTimeout(() -> emitterList.remove(sseEmitter));
        return sseEmitter;
    }

    public void sendSeatStatusBySse(String seatStatusJson){
        for(SseEmitter emitter : emitterList){
            try{
                emitter.send(SseEmitter.event()
                        .data(seatStatusJson, MediaType.APPLICATION_JSON));
            }catch (IOException e){
                emitterList.remove(emitter);
            }
        }
    }
}
