package com.linked.classbridge.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서부 오류가 발생했습니다."),

    ALREADY_REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "이미 등록된 이메일 입니다."),
    HELLO_NAME_IS_REQUIRED(HttpStatus.BAD_REQUEST, "Hello 이름은 필수입니다."),
    HELLO_NOT_FOUND(HttpStatus.BAD_REQUEST, "Hello를 찾을 수 없습니다."),

    ;
    private final HttpStatus httpStatus;
    private final String description;
}
