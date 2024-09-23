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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    private String userName;
    private String password;

    @Column(unique = true)
    private String nickname;

    @Description("예약 상태")
    @Builder.Default
    private Boolean hasReservation = false;

    public void reserveUser() {
        this.hasReservation = true;
    }

    public void cancelUserReservation() {
        this.hasReservation = false;
    }

}
