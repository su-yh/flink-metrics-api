-- 对于flink 环境的相关配置
-- DROP TABLE if EXISTS flink_env_config;
CREATE TABLE flink_env_config
(
    id             BIGINT AUTO_INCREMENT COMMENT '主键',
    flink_env_name VARCHAR(20) NOT NULL COMMENT 'flink 集群环境名',
    flink_web_host VARCHAR(20) NOT NULL COMMENT 'flink web 主机地址',
    flink_web_port INT         NOT NULL COMMENT 'flink web 端口',
    enable         TINYINT(1) DEFAULT 1 COMMENT '启用/禁用',

    created        DATETIME(3) DEFAULT CURRENT_TIMESTAMP (3) COMMENT '创建时间',
    updated        DATETIME(3) DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3),
    PRIMARY KEY (id)
) ENGINE = innodb COMMENT "taskManager 指标记录表";

-- 唯一索引
ALTER TABLE flink_env_config
    ADD UNIQUE INDEX uni_flink_env_config_n(flink_env_name);
