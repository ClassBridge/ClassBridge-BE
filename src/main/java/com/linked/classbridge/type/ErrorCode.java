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
    NOT_AUTHENTICATED_USER(HttpStatus.BAD_REQUEST, "인증된 사용자가 없습니다."),
    UNEXPECTED_PRINCIPAL_TYPE(HttpStatus.BAD_REQUEST, "예상하지 못한 Principal 타입입니다."),
    REFRESH_TOKEN_NULL(HttpStatus.BAD_REQUEST, "Refresh 토큰이 없습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "Refresh 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 Refresh 토큰입니다."),

    ALREADY_REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "이미 등록된 이메일 입니다."),
    ALREADY_EXIST_NICKNAME(HttpStatus.BAD_REQUEST, "이미 존재하는 닉네임 입니다."),
    NOT_SUPPORTED_AUTH_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 로그인 방식입니다."),
    ALREADY_REGISTERED_OTHER_AUTH_TYPE(HttpStatus.BAD_REQUEST, "다른 로그인 방식으로 가입한 사용자입니다."),
    ALREADY_EXIST_USER(HttpStatus.BAD_REQUEST, "이미 존재하는 사용자입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    SESSION_DOES_NOT_CONTAIN_CUSTOM_OAUTH2_USER(HttpStatus.BAD_REQUEST, "세션에 CustomOAuth2User가 존재하지 않습니다."),
    REQUIRED_USER_INFO(HttpStatus.BAD_REQUEST, "가입시 필요한 사용자 필수 정보가 빠져있습니다."),
    NO_INFORMATION_TO_UPDATE(HttpStatus.BAD_REQUEST, "수정할 정보가 없습니다."),

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
    KAKAO_MAP_ERROR(HttpStatus.BAD_REQUEST, "카카오맵 api 불러오기 중 실패했습니다."),
    CANNOT_CONVERT_FILE(HttpStatus.BAD_REQUEST, "파일 변환에 실패했습니다."),
    FILE_DELETE_FAILED(HttpStatus.BAD_REQUEST, "파일 제거에 실패했습니다."),
    CLASS_NOT_FOUND(HttpStatus.BAD_REQUEST, "원데이 클래스를 찾을 수 없습니다."),
    MISMATCH_USER_TAG(HttpStatus.BAD_REQUEST, "태그를 작성한 유저와 로그인한 유저가 일치하지 않습니다.."),
    MISMATCH_CLASS_TAG(HttpStatus.BAD_REQUEST, "해당 클래스와 태그의 클래스가 일치하지 않습니다."),
    CANNOT_FOUND_TAG(HttpStatus.BAD_REQUEST, "해당 태그를 찾을 수 없습니다."),
    CLASS_HAVE_MAX_TAG(HttpStatus.BAD_REQUEST, "클래스 태그는 최대 5개를 등록할 수 있습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "카테고리를 찾을 수 없습니다"),
    CANNOT_CHANGE_END_DATE_CAUSE_RESERVED_PERSON_EXISTS(HttpStatus.BAD_REQUEST, "변경할 종료일 이후의 레슨에 예약된 사람이 존재합니다."),
    CANNOT_CHANGE_START_DATE(HttpStatus.BAD_REQUEST, "시작일을 변경할 수 없습니다."),
    CANNOT_DELETE_CLASS_CAUSE_RESERVED_PERSON_EXISTS(HttpStatus.BAD_REQUEST, "현재 날짜 이후의 레슨에 예약된 사람이 존재합니다."),

    CANNOT_FOUND_FAQ(HttpStatus.BAD_REQUEST, "해당 FAQ를 찾을 수 없습니다."),
    MISMATCH_CLASS_FAQ(HttpStatus.BAD_REQUEST, "클래스 id와 faq의 클래스 id가 일치하지 않습니다."),
    MISMATCH_USER_FAQ(HttpStatus.BAD_REQUEST, "해당 유저와 faq 작성자가 다릅니다."),
    MISMATCH_USER_CLASS(HttpStatus.BAD_REQUEST, "해당 유저와 클래스 생성자가 다릅니다."),
    EXISTS_LESSON_DATE_START_TIME(HttpStatus.BAD_REQUEST, "이미 존재하는 레슨날짜와 시간입니다."),
    LESSON_DATE_MUST_BE_AFTER_NOW(HttpStatus.BAD_REQUEST, "레슨 날짜는 현재 날짜 이후로만 만들 수 있습니다."),
    MISMATCH_CLASS_LESSON(HttpStatus.BAD_REQUEST, "클래스와 해당 레슨의 클래스가 일치하지 않습니다."),
    MISMATCH_USER_LESSON(HttpStatus.BAD_REQUEST, "레슨 생성자와 로그인 유저가 일치하지 않습니다."),
    EXISTS_RESERVED_PERSON(HttpStatus.BAD_REQUEST, "해당 레슨에 예약자가 존재합니다."),
    CLASS_HAVE_MAX_FAQ(HttpStatus.BAD_REQUEST, "클래스는 최대 5개의 FAQ를 만들 수 있습니다."),
    INVALIDATE_CLASS_NAME(HttpStatus.BAD_REQUEST, "클래스 이름은 2자 이상 20자 이하로 작성해주세요."),
    INVALIDATE_CLASS_INTRODUCTION(HttpStatus.BAD_REQUEST, "클래스 설명은 20자 이상 500자 이하로 작성해주세요."),
    INVALIDATE_CLASS_PERSONAL(HttpStatus.BAD_REQUEST, "최대 인원을 변경할 수 없습니다."),
    MAX_PARTICIPANTS_EXCEEDED(HttpStatus.BAD_REQUEST, "최대 인원을 초과했습니다."),

    RESERVATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 예약입니다."),

    PAY_ERROR(HttpStatus.BAD_REQUEST, "결제 요청에 실패했습니다."),
    PAY_CANCEL(HttpStatus.BAD_REQUEST, "결제 요청을 취소합니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다."),
    INVALID_PAYMENT_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 결제 정보입니다."),
    NULL_RESPONSE_FROM_PAYMENT_GATEWAY(HttpStatus.BAD_REQUEST, "결제 게이트웨이 응답이 없습니다."),
    MISSING_PAY_RESPONSE_IN_SESSION(HttpStatus.BAD_REQUEST,"세션에 결제 정보가 없습니다."),

    CHAT_ROOM_NOT_FOUND(HttpStatus.BAD_REQUEST, "채팅방을 찾을 수 없습니다."),
    INVALID_REFUND_QUANTITY(HttpStatus.BAD_REQUEST,"환불 가능 수량을 확인해주세요."),

    INVALID_RESERVATION_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 예약입니다."),
    NO_REFUND_AVAILABLE(HttpStatus.BAD_REQUEST, "환불 가능 금액이 아닙니다."),
    ;

    private final HttpStatus httpStatus;
    private final String description;
}
