package com.suyh.metric.mvc.exception;

import lombok.Getter;
import org.springframework.lang.NonNull;

@Getter
public class BaseException extends AbstractBusinessException {

    public BaseException(@NonNull ExceptionCategory category, int code, Object... params) {
        super(category, code, params);
    }
}
