package com.linked.classbridge.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseMessage {
    HELLO_GET_SUCCESS("Hello 조회 성공"),
    HELLO_REGISTER_SUCCESS("Hello 등록 성공"),
    HELLO_UPDATE_SUCCESS("Hello 수정 성공"),
    REVIEW_REGISTER_SUCCESS("리뷰 등록 성공"),
    REVIEW_UPDATE_SUCCESS("리뷰 수정 성공"),
    REVIEW_DELETE_SUCCESS("리뷰 삭제 성공"),
    NO_MATCHED_NICKNAME("일치하는 닉네임이 없습니다"),
    NO_MATCHED_EMAIL("일치하는 이메일이 없습니다"),
    SIGNUP_SUCCESS("회원가입 성공"),
    LOGIN_SUCCESS("로그인 성공"),
    ONE_DAY_CLASS_LIST_GET_SUCCESS("강사 클래스 리스트 조회 성공"),
    ONE_DAY_CLASS_GET_SUCCESS("강사 클래스 조회 성공"),
    CLASS_REGISTER_SUCCESS("클래스 등록 성공"),
    CLASS_DELETE_SUCCESS("클래스 삭제 성공"),
    REVIEW_GET_SUCCESS("리뷰 조회 성공"),

    CLASS_UPDATE_SUCCESS("클래스 세부 정보 수정 성공"),
    CLASS_FAQ_UPDATE_SUCCESS("클래스 FAQ 수정 성공"),
    CLASS_FAQ_DELETE_SUCCESS("클래스 FAQ 삭제 성공"),
    CLASS_TAG_UPDATE_SUCCESS("클래스 Tag 수정 성공"),
    CLASS_TAG_DELETE_SUCCESS("클래스 Tag 삭제 성공"),

    PAYMENT_SUCCESS("결제 승인"),

    ACCESS_TOKEN_ISSUED("Access 토큰 발급 성공"),

    ;

    private final String message;
}
