package com.suyh.metric.mp.mybatis;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// 分页参数
@NoArgsConstructor
@Data
public class PageParam implements Serializable {
    private static final long serialVersionUID = -4369470413351363288L;

    public static final Integer PAGE_NO = 1;
    public static final Integer PAGE_SIZE = 10;

    // 页码最小值为 1
    private Integer pageNo = PAGE_NO;

    // 每页条数不能为空
    // 每页条数最小值为 1
    // 每页条数最大值为 100
    private Integer pageSize = PAGE_SIZE;

}
