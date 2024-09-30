package kr.co.ureca.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import kr.co.ureca.dto.SeatResponse;
import kr.co.ureca.entity.Seat;
import kr.co.ureca.entity.User;
import kr.co.ureca.exception.CustomException;
import kr.co.ureca.exception.ErrorCode;
import kr.co.ureca.repository.SeatRepository;
import kr.co.ureca.repository.UserRepository;
import kr.co.ureca.sse.SseService;
import kr.co.ureca.websocket.WebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReservationService {

    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;
//    private final WebSocketHandler webSocketHandler;
    private final SseService sseService;

    @PostConstruct
    public void initSeats() {
        if (seatRepository.count() == 0) {
            for (int i = 1; i <= 30; i++) {
                Seat seat = Seat.builder()
                        .seatNo((long) i)
                        .build();
                seatRepository.save(seat);
            }
        }
        log.info("좌석을 생성했습니다.");
    }

    public List<SeatResponse> getAllSeats(){
        ArrayList<SeatResponse> seatResponses = new ArrayList<>();
        List<Seat> seatList = seatRepository.findAll();
        for (Seat seat:seatList){
            SeatResponse seatResponse = SeatResponse.builder()
                    .seatNo(seat.getSeatNo())
                    .status(seat.getStatus())
                    .userName(seat.getUser().getUserName())
                    .build();
            seatResponses.add(seatResponse);
        }
        return seatResponses;
    }

    public Long getMyReservation(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));
        return seatRepository.findSeatByUser(user)
                .map(Seat::getSeatNo)
                .orElse(null);
    }

    public Seat reserve(Long seatNo,Long userId) {
        //좌석 번호를 기준으로 락 설정
        RLock lock = redissonClient.getLock("seatLock:" + seatNo);

        try {
            boolean isLocked = lock.tryLock(0, 10, TimeUnit.SECONDS);
            //락 확보 실패시 예외
            if (!isLocked){
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED,HttpStatus.BAD_REQUEST);
            }

            Seat seat = seatRepository.findBySeatNo(seatNo)
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, HttpStatus.BAD_REQUEST));

            if (seat.getStatus()) {
                throw new CustomException(ErrorCode.RESERVED_SEAT, HttpStatus.BAD_REQUEST);
            }

            if (user.getHasReservation()) {
                throw new CustomException(ErrorCode.RESERVED_USER, HttpStatus.BAD_REQUEST);
            }

            user.reserveUser();
            seat.reserveSeat(user);

            userRepository.save(user);
            seatRepository.save(seat);

            streamSeatStatus(seat);
            log.info("User in Seat after saving: {}", seat.getUser().getUserName());

            return seat;
        }catch (InterruptedException e) {
            throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED, HttpStatus.BAD_REQUEST);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock(); // 반드시 락을 해제해야 함
            }
        }

    }

    public Seat deleteReservation(Long seatNo,Long userId){
        Seat seat = seatRepository.findBySeatNo(seatNo)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 좌석입니다. 좌석번호: " + seatNo));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, HttpStatus.BAD_REQUEST));

        if(seat.getStatus().equals(true)) {
            if (seat.getUser().equals(user)) {
                seat.cancelSeatReservation();
                user.cancelUserReservation();

                seatRepository.save(seat);
                userRepository.save(user);

                streamSeatStatus(seat);
            } else {
                throw new CustomException(ErrorCode.UNAUTHORIZED_USER, HttpStatus.BAD_REQUEST);
            }
        }else{
            throw new CustomException(ErrorCode.RESERVED_SEAT, HttpStatus.BAD_REQUEST);
        }

        return seat;
    }

    public void streamSeatStatus(Seat seat) {
        ObjectMapper objectMapper = new ObjectMapper();
        SeatResponse.SeatResponseBuilder seatResponseBuilder = SeatResponse.builder()
                .seatNo(seat.getSeatNo())
                .status(seat.getStatus());
        if (seat.getUser()!=null){
            seatResponseBuilder.userName(seat.getUser().getUserName());
        }
        SeatResponse seatResponse = seatResponseBuilder.build();

        try {
            String seatStatusJson = objectMapper.writeValueAsString(seatResponse);
            sseService.broadcastSeatStatusBySse(seatStatusJson);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
}
