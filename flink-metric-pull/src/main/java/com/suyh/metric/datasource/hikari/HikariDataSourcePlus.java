package com.suyh.metric.datasource.hikari;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import javax.validation.Valid;

/**
 * @author suyh
 * @since 2025-02-18
 */
@Data
public class HikariDataSourcePlus extends HikariDataSource {
    @NestedConfigurationProperty
    @Valid
    private final FlywayProperties flyway = new FlywayProperties();
}
