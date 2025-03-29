-- taskManager 指标记录表 目录来说只处理一个task manager 的指标，如果有多个task manager 暂时还处理不了。
-- DROP TABLE if EXISTS tm_metric;
CREATE TABLE tm_metric
(
    id                                BIGINT AUTO_INCREMENT COMMENT '主键',
    flink_env_name                    VARCHAR(20) NOT NULL COMMENT 'flink 环境名称',
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
    flink_memory_manager_used         BIGINT,
    flink_memory_manager_total        BIGINT,
    jvm_memory_metaspace_used         BIGINT,
    jvm_memory_metaspace_max          BIGINT,


    created                           DATETIME(3) DEFAULT CURRENT_TIMESTAMP (3) COMMENT '创建时间',
    updated                           DATETIME(3) DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3),
    PRIMARY KEY (id)
) ENGINE = innodb COMMENT "taskManager 指标记录表";

ALTER TABLE tm_metric
    ADD UNIQUE INDEX uni_tm_metric_n_t(flink_env_name, ts);
