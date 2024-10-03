package kr.co.ureca.dto;

import lombok.Builder;

@Builder
public record SeatResponse(
        long seatNo,
        boolean status,
        String userName
) {
}
