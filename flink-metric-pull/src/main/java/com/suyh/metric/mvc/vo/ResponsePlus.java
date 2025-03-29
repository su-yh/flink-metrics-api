package com.suyh.metric.mvc.vo;

import lombok.Getter;

/**
 * 额外的数据记录
 *
 * @author suyh
 * @since 2024-10-16
 */
@Getter
public class ResponsePlus<T, S> extends ResponseResult<T> {
    private final S summary;

    public ResponsePlus(T data, S summary) {
        super(ResponseResult.SUCCESS_CODE, ResponseResult.SUCCESS_MSG, data);
        this.summary = summary;
    }
}
