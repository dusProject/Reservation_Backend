package kr.co.ureca.service;

import kr.co.ureca.dto.DeleteReservationRequest;
import kr.co.ureca.dto.ReservationRequest;
import kr.co.ureca.entity.Seat;
import kr.co.ureca.entity.User;
import kr.co.ureca.exception.CustomException;
import kr.co.ureca.exception.ErrorCode;
import kr.co.ureca.repository.SeatRepository;
import kr.co.ureca.repository.UserRepository;
import kr.co.ureca.sse.SseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    UserRepository mockUserRepository;

    @Mock
    private SeatRepository mockSeatRepository;

    @Autowired
    private SeatRepository seatRepository;

    @InjectMocks
    ReservationService reservationService;

    @Mock
    private RedissonClient mockRedissonClient;

    @Mock
    private RLock mockRLock;

    @Mock
    private SseService mockSseService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() throws InterruptedException {
        MockitoAnnotations.openMocks(this);
        reservationService = new ReservationService(mockSeatRepository, mockUserRepository, mockRedissonClient, mockSseService);

        // 불필요한 모킹도 허용
        lenient().when(mockRedissonClient.getLock(anyString())).thenReturn(mockRLock);

        lenient().when(mockRLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);

    }

    @Test
    void initSeats() {
        //given
        //when
        long count = seatRepository.count();
        //then
        assertThat(count).isEqualTo(30);

    }

    @Test
    @DisplayName("예약 성공")
    void reserve() {
        //given

        Seat seat = Seat.builder().seatNo(1L).status(false).build();  // 예약 가능한 좌석
        User user = User.builder().userId(1L).nickname("userNickName").userName("userName").password("password").build();

        when(mockSeatRepository.findBySeatNo(1L)).thenReturn(Optional.of(seat));
        when(mockUserRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(mockSseService).broadcastSeatStatusBySse(anyString());


        //when
        Seat reservedSeat = reservationService.reserve(1L,1L);

        //then
        assertThat(reservedSeat.getStatus()).isTrue();
        assertThat(reservedSeat.getUser().getUserId()).isEqualTo(1L);
        verify(mockUserRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("이미 예약된 좌석은 예약 불가")
    void ifReservedSeatThrowException() {
        //given
        Seat reservedSeat = Seat.builder().seatNo(1L).status(true).build();  // 이미 예약된 좌석
        User user = User.builder().userId(1L).hasReservation(false).build();
        when(mockSeatRepository.findBySeatNo(1L)).thenReturn(Optional.of(reservedSeat));
        when(mockUserRepository.findById(1L)).thenReturn(Optional.of(user));

        //when
        CustomException exception = assertThrows(CustomException.class, () -> reservationService.reserve(1L,1L));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESERVED_SEAT);
        verify(mockUserRepository, never()).save(any(User.class));
        verify(mockSeatRepository, never()).save(any(Seat.class));
    }

    @Test
    @DisplayName("이미 예약한 사용자는 예약 불가")
    void ifReservedUserThrowException() {
        //given
        Seat seat = Seat.builder().seatNo(1L).status(false).build();  // 예약 가능한 좌석
        User reservedUser = User.builder().userId(1L).nickname("userNickName").userName("userName").password("1111").hasReservation(true).build();  // 이미 예약한 사용자

        when(mockSeatRepository.findBySeatNo(1L)).thenReturn(Optional.of(seat));
        when(mockUserRepository.findById(1L)).thenReturn(Optional.of(reservedUser));

        //when //then
        CustomException exception = assertThrows(CustomException.class, () -> reservationService.reserve(1L,1L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESERVED_USER);
        verify(mockUserRepository, never()).save(any(User.class));
        verify(mockSeatRepository, never()).save(any(Seat.class));
    }

    @Test
    @DisplayName("삭제 성공")
    void deleteReservationSuccess() {
        // Given
        User user = User.builder().userId(1L).nickname("userNickname").userName("userName").hasReservation(true).build();
        Seat seat = Seat.builder().seatNo(1L).user(user).status(true).build();

        when(mockSeatRepository.findBySeatNo(eq(1L))).thenReturn(Optional.of(seat));
        when(mockUserRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(mockSseService).broadcastSeatStatusBySse(anyString());

        // When
        Seat deletedSeat = reservationService.deleteReservation(1L,1L);

        // Then
        verify(mockUserRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNull(deletedSeat.getUser());
        assertFalse(deletedSeat.getStatus());
        assertThat(capturedUser.getHasReservation()).isFalse();

        verify(mockSeatRepository, times(1)).findBySeatNo(eq(1L));
        verify(mockSeatRepository, times(1)).save(deletedSeat);
    }

    @Test
    @DisplayName("나의 예약에서 예약 정보가 없을시 null 반환")
    void getMyReservation() {
        //Given
        User user = User.builder().userId(1L).nickname("userNickname").userName("userName").hasReservation(false).build();
        Seat seat = Seat.builder().seatNo(1L).status(false).build();
        when(mockUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mockSeatRepository.findSeatByUser(user)).thenReturn(Optional.empty());
        //When
        Long seatNo = reservationService.getMyReservation(1L);
        //Then
        assertNull(seatNo);
    }
}