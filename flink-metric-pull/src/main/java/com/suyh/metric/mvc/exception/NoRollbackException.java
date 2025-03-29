package com.suyh.metric.mvc.exception;

import org.springframework.lang.NonNull;

/**
 * @author suyh
 * @since 2024-10-21
 */
public class NoRollbackException extends AbstractBusinessException {
    public NoRollbackException(@NonNull ExceptionCategory category, int code, Object... params) {
        super(category, code, params);
    }
}
