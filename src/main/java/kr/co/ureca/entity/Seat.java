package kr.co.ureca.entity;

import jakarta.persistence.*;
import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    @Description("예약 상태")
    @Builder.Default
    private Boolean status = false;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    private Long seatNo;

    public void reserveSeat(User user) {
        this.user = user;
        this.status = true;
    }

    public void cancelSeatReservation() {
        this.user = null;
        this.status = false;
    }

}
