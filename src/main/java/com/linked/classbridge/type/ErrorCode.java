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

    ALREADY_EXIST_NICKNAME(HttpStatus.BAD_REQUEST, "이미 존재하는 닉네임 입니다."),
    NOT_SUPPORTED_AUTH_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 로그인 방식입니다."),
    ALREADY_REGISTERED_OTHER_AUTH_TYPE(HttpStatus.BAD_REQUEST, "다른 로그인 방식으로 가입한 사용자입니다."),
    ALREADY_EXIST_USER(HttpStatus.BAD_REQUEST, "이미 존재하는 사용자입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    HELLO_NAME_IS_REQUIRED(HttpStatus.BAD_REQUEST, "Hello 이름은 필수입니다."),
    HELLO_NOT_FOUND(HttpStatus.BAD_REQUEST, "Hello를 찾을 수 없습니다."),

    REVIEW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 리뷰를 작성하셨습니다."),
    INVALID_ONE_DAY_CLASS_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 클래스 ID 입니다."),
    INVALID_REVIEW_RATING(HttpStatus.BAD_REQUEST, "리뷰 평점은 1점부터 5점까지 가능합니다."),
    INVALID_REVIEW_CONTENTS(HttpStatus.BAD_REQUEST, "리뷰 내용은 10자 이상 200자 이하로 작성해주세요."),
    INVALID_IMAGE_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "유효한 이미지 파일이 아닙니다."),
    FAILED_TO_UPLOAD_IMAGE(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
    FAILED_TO_DELETE_IMAGE(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다."),


    LESSON_NOT_FOUND(HttpStatus.BAD_REQUEST, "클래스를 찾을 수 없습니다."),
    REVIEW_NOT_FOUND(HttpStatus.BAD_REQUEST, "리뷰를 찾을 수 없습니다."),
    NOT_REVIEW_OWNER(HttpStatus.FORBIDDEN, "리뷰 작성자만 수정 및 삭제가 가능합니다."),
    ;
    private final HttpStatus httpStatus;
    private final String description;
}
