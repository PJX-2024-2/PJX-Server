package com.pjx.pjxserver.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.pjx.pjxserver.common.exception.ErrorStatus.*;


@Getter
@AllArgsConstructor
public enum CommonErrorCode {

    INTERNAL_SERVER_ERROR(INTERNAL_SERVER, "0000", "내부 서버 에러입니다."),
    DB_ACCESS_ERROR(INTERNAL_SERVER, "0001", "DB 에러입니다."),
    INVALID_REQUEST_CONTENT(BAD_REQUEST, "0002", "잘못된 요청 내용입니다."),
    ENUM_ERROR(BAD_REQUEST, "0003", "잘못된 Enum 값 입니다."),
    NOT_FOUND_ERROR(NOT_FOUND, "0004", "잘못된 API 요청입니다.");

    private final ErrorStatus status;
    private final String code;
    private final String message;

}
