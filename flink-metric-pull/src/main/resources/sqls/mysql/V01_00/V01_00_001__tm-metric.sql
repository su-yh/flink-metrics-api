-- taskManager 指标记录表
-- DROP TABLE if EXISTS tm_metric;
CREATE TABLE tm_metric
(
    id                                BIGINT AUTO_INCREMENT COMMENT '主键',
--    host                              VARCHAR(20) NOT NULL COMMENT '主机IP 地址',
--    port                              INT         NOT NULL COMMENT '端口',
    task_manager_id                   VARCHAR(50) NOT NULL COMMENT 'task_manager_id',
    ts                                BIGINT      NOT NULL COMMENT '时间戳',
    heap_used                         BIGINT,
    heap_committed                    BIGINT,
    heap_max                          BIGINT,
    non_heap_used                     BIGINT,
    non_heap_committed                BIGINT,
    non_heap_max                      BIGINT,
    direct_count                      BIGINT,
    direct_used                       BIGINT,
    direct_max                        BIGINT,
    mapped_count                      BIGINT,
    mapped_used                       BIGINT,
    mapped_max                        BIGINT,
    shuffle_memory_segments_available BIGINT,
    shuffle_memory_segments_used      BIGINT,
    shuffle_memory_segments_total     BIGINT,
    shuffle_memory_available          BIGINT,
    shuffle_memory_used               BIGINT,
    shuffle_memory_total              BIGINT,

    created                           DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated                           DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id)
) ENGINE = innodb COMMENT "taskManager 指标记录表";

ALTER TABLE tm_metric
    ADD INDEX idx_tm_metric_t_t(task_manager_id, ts);
