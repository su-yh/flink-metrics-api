package com.suyh.metric.constant.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum YesOrNoEnums {
    YES(1),
    NO(0),
    ;

    @EnumValue
    private final int code;

    YesOrNoEnums(int code) {
        this.code = code;
    }
}