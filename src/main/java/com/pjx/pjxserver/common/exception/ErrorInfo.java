package com.pjx.pjxserver.common.exception;

import lombok.Getter;

@Getter
public class ErrorInfo<T> {

    private final String code;
    private final String message;
    private T info;

    public ErrorInfo(final String code, final String message, final T info) {
        this.code = code;
        this.message = message;
        this.info = info;
    }

    public ErrorInfo(final String code, final String message) {
        this.code = code;
        this.message = message;
    }

}
