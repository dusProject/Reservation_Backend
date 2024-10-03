package kr.co.ureca.controller;

import kr.co.ureca.sse.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class HomeSSEController {
    private final SseService sseService;

    @GetMapping("/home/sse")
    public SseEmitter streamSeatStatus(){
        return sseService.createEmitter();
    }

    @GetMapping("/reservation/sse")
    public SseEmitter streamReservationStatus(){
        return sseService.createEmitter();
    }

}
