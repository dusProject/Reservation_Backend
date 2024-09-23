package kr.co.ureca.controller;

import kr.co.ureca.sse.SseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class HomeSSEController {
    private final SseHandler sseHandler;

    @GetMapping("/home/sse")
    public SseEmitter streamSeatStatus(){
        return sseHandler.createEmitter();
    }

}
