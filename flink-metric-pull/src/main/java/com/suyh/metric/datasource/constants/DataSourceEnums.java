package com.suyh.metric.datasource.constants;

import lombok.Getter;

/**
 * @author suyh
 * @since 2025-03-18
 */
@Getter
public enum DataSourceEnums {
    DEFAULT(DataSourceNames.DEFAULT),
    ;

    private final String code;

    DataSourceEnums(String code) {
        this.code = code;
    }
}
