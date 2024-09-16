package kr.co.ureca.dto;

import lombok.Builder;

@Builder
public record ReservationRequest(
        String userName,
        String password,
        String nickname,
        long seatNo
) {
}
