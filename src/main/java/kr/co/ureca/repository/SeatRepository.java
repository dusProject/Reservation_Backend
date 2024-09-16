package kr.co.ureca.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import kr.co.ureca.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "100")
    })
    Optional<Seat> findBySeatNo(Long seatNo);
}
