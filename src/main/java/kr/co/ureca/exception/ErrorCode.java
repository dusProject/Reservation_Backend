package kr.co.ureca.exception;

import lombok.Getter;

@Getter
public enum ErrorCode implements EnumModel{

    //Common
    RESOURCE_NOT_FOUND(40400, "CM001", "존재하지 않습니다."),
    LOCK_ACQUISITION_FAILED(40401,"CM002","Lock 확보에 실패했습니다."),
    //Conflict
    RESERVED_SEAT(40900,"CF001","이미 예약된 좌석입니다. 다른 좌석을 선택해주세요."),
    RESERVED_USER(40901,"CF002","이미 예약한 좌석이 존재합니다. 기존 예약을 취소해주세요."),
    UNAUTHORIZED_USER(40300,"A001","예약자 정보와 일치하지 않습니다."),
    USER_NOT_FOUND(40301,"A002","가입되지 않은 사용자입니다."),
    UNVALID_DELETE_REQUEST(40000,"B001","예약된 좌석이 아닙니다."),
    //Server
    SERVER_ERROR(50000,"S001","서버 오류입니다.");
    private int status;
    private String code;
    private String message;
    private String detail;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    @Override
    public String getKey() {
        return this.code;
    }

    @Override
    public String getValue() {
        return this.message;
    }

}
