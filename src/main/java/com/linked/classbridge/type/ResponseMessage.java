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
    ;

    private final String message;
}
