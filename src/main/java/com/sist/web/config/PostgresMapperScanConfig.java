package com.sist.web.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.sist.web.postgres",sqlSessionFactoryRef="postgresSqlSessionFactory")
public class PostgresMapperScanConfig {

}
