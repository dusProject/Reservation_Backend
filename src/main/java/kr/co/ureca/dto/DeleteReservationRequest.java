package kr.co.ureca.dto;

import lombok.Builder;

@Builder
public record DeleteReservationRequest(
        String userName,
        String password,
        String nickname,
        long seatNo
) {
}
