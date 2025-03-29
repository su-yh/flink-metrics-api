package com.suyh.metric.mvc.vo;

import lombok.Getter;

@Getter
public class ResponseResult<T> {
    public static final Integer SUCCESS_CODE = 0;
    public static final String SUCCESS_MSG = "SUCCESS";

    private final Integer code;

    private final String message;

    private final T data;

    protected ResponseResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseResult<T> ofSuccess(T data, String message) {
        return new ResponseResult<>(SUCCESS_CODE, message, data);
    }

    public static <T> ResponseResult<T> ofSuccess(T data) {
        return new ResponseResult<>(SUCCESS_CODE, SUCCESS_MSG, data);
    }

    public static <T> ResponseResult<T> ofSuccess() {
        return new ResponseResult<>(SUCCESS_CODE, SUCCESS_MSG, null);
    }

    public static <T> ResponseResult<T> ofFail(int code, String message) {
        return new ResponseResult<>(code, message, null);
    }
}
