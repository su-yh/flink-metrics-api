package com.suyh.metric.mvc.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

/**
 * @author suyh
 * @since 2024-10-21
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractBusinessException extends RuntimeException {
    private static final long serialVersionUID = -4365987799788427525L;

    private final int code;

    private final ExceptionCategory category;

    // errorCode 中msg 占位符中对应的参数
    private final Object[] params;

    public AbstractBusinessException(@NonNull ExceptionCategory category, int code, Object... params) {
        // 这样将会在message 里面显示该值。
        super(category.name() + " EXCEPTION");

        this.category = category;
        this.code = code;
        this.params = params;
        // 这个是使用log4j 来解析 {} 的方法，注释掉，但不要删除，免得找不到了。
        // String message = org.slf4j.helpers.MessageFormatter.arrayFormat(errorCode.getMsg(), params).getMessage();
    }
}
