package com.sist.web.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {
	@Bean(name="oracleDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.oracle")
	public DataSource oracleDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean(name="postgresDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.postgres")
	public DataSource postgresDataSource() {
		return DataSourceBuilder.create().build();
	}
}
