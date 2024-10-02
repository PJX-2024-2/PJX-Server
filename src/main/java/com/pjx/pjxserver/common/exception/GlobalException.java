package com.pjx.pjxserver.common.exception;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {

    private final ErrorStatus status;
    private final ErrorInfo<?> errorInfo;

    public GlobalException(ErrorStatus status, ErrorInfo<?> errorInfo) {
        super(errorInfo.getMessage());
        this.status = status;
        this.errorInfo = errorInfo;
    }
}