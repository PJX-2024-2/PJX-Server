package com.pjx.pjxserver.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"success", "status", "code", "message", "info"})
public class ErrorResponse<T> {

    private final boolean success = false;
    private final int status;
    private final String code;
    private final String message;
    private final T info;


    @Builder
    public ErrorResponse(int status, ErrorInfo<T> errorInfo) {
        this.status = status;
        this.code = errorInfo.getCode();
        this.message = errorInfo.getMessage();
        this.info = errorInfo.getInfo();
    }

    public static <T> ErrorResponse<T> of(ErrorStatus status, ErrorInfo<T> errorInfo) {
        return ErrorResponse.<T>builder()
                .status(status.getValue())
                .errorInfo(errorInfo)
                .build();
    }

}
