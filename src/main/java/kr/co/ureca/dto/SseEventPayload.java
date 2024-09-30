package kr.co.ureca.dto;

import lombok.Builder;

@Builder
public record SseEventPayload(
        long seatNo,
        boolean seatStatus
) {
}
