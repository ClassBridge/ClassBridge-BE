package com.linked.classbridge.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseMessage {
    REVIEW_REGISTER_SUCCESS("리뷰 등록 성공"),
    REVIEW_UPDATE_SUCCESS("리뷰 수정 성공"),
    REVIEW_DELETE_SUCCESS("리뷰 삭제 성공"),
    NO_MATCHED_NICKNAME("사용 가능한 닉네임 입니다"),
    NO_MATCHED_EMAIL("사용 가능한 이메일 입니다"),
    SIGNUP_SUCCESS("회원가입 성공"),
    LOGIN_SUCCESS("로그인 성공"),
    ONE_DAY_CLASS_LIST_GET_SUCCESS("강사 클래스 리스트 조회 성공"),
    ONE_DAY_CLASS_GET_SUCCESS("강사 클래스 조회 성공"),
    CLASS_REGISTER_SUCCESS("클래스 등록 성공"),
    CLASS_DELETE_SUCCESS("클래스 삭제 성공"),
    REVIEW_GET_SUCCESS("리뷰 조회 성공"),

    CLASS_UPDATE_SUCCESS("클래스 세부 정보 수정 성공"),
    CLASS_FAQ_REGISTER_SUCCESS("클래스 FAQ 추가 성공"),
    CLASS_FAQ_UPDATE_SUCCESS("클래스 FAQ 수정 성공"),
    CLASS_FAQ_DELETE_SUCCESS("클래스 FAQ 삭제 성공"),
    CLASS_TAG_REGISTER_SUCCESS("클래스 Tag 추가 성공"),
    CLASS_TAG_UPDATE_SUCCESS("클래스 Tag 수정 성공"),
    CLASS_TAG_DELETE_SUCCESS("클래스 Tag 삭제 성공"),
    CLASS_LESSON_REGISTER_SUCCESS("클래스 레슨 추가 성공"),
    CLASS_LESSON_UPDATE_SUCCESS("클래스 레슨 수정 성공"),
    CLASS_LESSON_DELETE_SUCCESS("클래스 레슨 삭제 성공"),

    WISH_GET_SUCCESS("Wish 리스트 조회 성공"),
    WISH_ADD_SUCCESS("Wish 추가 성공"),
    WISH_DELETE_SUCCESS("Wish 삭제 성공"),

    ONE_DAY_CLASS_SEARCH_SUCCESS("검색 성공"),
    ONE_DAY_CLASS_AUTO_COMPLETE_SUCCESS("자동 완성 성공"),

    PAYMENT_SUCCESS("결제 승인"),
    ACCESS_TOKEN_ISSUED("Access 토큰 발급 성공"),
    USER_UPDATE_SUCCESS("사용자 정보 수정 성공"),
    VALIDATE_BUSINESS_REGISTRATION_NUMBER_SUCCESS("유효한 사업자등록번호"),
    NOT_VALID_BUSINESS_REGISTRATION_NUMBER("유효하지 않은 사업자등록번호"),
    TUTOR_REGISTER_SUCCESS("강사 등록 성공"),
    TUTOR_UPDATE_SUCCESS("강사 정보 수정 성공"),

    RESERVATION_SUCCESS("예약 생성 성공"),
    PAYMENT_GET_SUCCESS("결제 조회 성공"),

    RESERVATION_REGISTER_SUCCESS("예약 생성 성공"),
    RESERVATION_GET_SUCCESS("예약 조회 성공"),
    RESERVATION_CANCELED_BY_TUTOR_SUCCESS("예약 취소 성공"),
    REFUND_SUCCESS("환불 승인"),
    REFUND_GET_SUCCESS("환불 조회 성공"),

    TUTOR_PAYMENT_GET_SUCCESS("정산 조회 성공"),

    CHAT_ROOM_CREATE_SUCCESS("채팅방 생성 성공"),
    CHAT_ROOM_JOIN_SUCCESS("채팅방 참여 성공"),
    CHAT_ROOM_LEAVE_SUCCESS("채팅방 퇴장하기 성공"),
    CHAT_ROOM_CLOSE_SUCCESS("채팅방 닫기 성공"),

    ATTENDANCE_CHECK_SUCCESS("출석 체크 성공"),
    GET_USER_BADGES_SUCCESS("사용자 뱃지 조회 성공"),
    UPLOAD_BADGE_SUCCESS("뱃지 등록 성공"),
    ;
    private final String message;
}
