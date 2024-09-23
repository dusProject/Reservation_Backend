package kr.co.ureca.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.ureca.dto.DeleteReservationRequest;
import kr.co.ureca.dto.ReservationRequest;
import kr.co.ureca.dto.SeatResponse;
import kr.co.ureca.entity.Seat;
import kr.co.ureca.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "좌석 예약 API ")
@RequiredArgsConstructor
@RestController
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "홈 화면", responses = {
            @ApiResponse(responseCode = "200", description = "홈화면 진입 성공"),
            @ApiResponse(responseCode = "400", description = "홈화면 진입 실패")
    })
    @GetMapping("/ureca")
    public ResponseEntity<List<SeatResponse>> home(){
        List<SeatResponse> seatList = reservationService.getAllSeats();
        return new ResponseEntity<>(seatList, HttpStatus.OK);
    }

    @Operation(summary = "좌석 예약", responses = {
            @ApiResponse(responseCode = "200", description = "예약 성공"),
            @ApiResponse(responseCode = "400", description = "예약 실패")
    })
    @PatchMapping("/ureca/reservation/{seatNo}")
    public ResponseEntity<Seat> reservation(@PathVariable Long seatNo,Long userId){
        Seat reservatedSeat = reservationService.reserve(seatNo,userId);
        return new ResponseEntity<>(reservatedSeat, HttpStatus.OK);
    }

    @Operation(summary = "예약 취소", responses = {
            @ApiResponse(responseCode = "200", description = "예약 취소 성공"),
            @ApiResponse(responseCode = "400", description = "예약 취소 실패")
    })
    @DeleteMapping("/ureca/myReservation")
    public ResponseEntity<Void> deleteReservation(Long seatNo,Long userId){
        reservationService.deleteReservation(seatNo,userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "홈 화면", responses = {
            @ApiResponse(responseCode = "200", description = "홈화면 진입 성공"),
            @ApiResponse(responseCode = "400", description = "홈화면 진입 실패")
    })
    @GetMapping("/ureca/myReservation")
    public ResponseEntity<Long> getMyReservation(Long userId){
        Long seatNo = reservationService.getMyReservation(userId);
        return new ResponseEntity<>(seatNo, HttpStatus.OK);
    }
}
