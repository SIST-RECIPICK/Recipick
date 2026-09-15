package com.sist.web.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration // 설정 어노테이션
public class PostgresMyBatisConfig {   //설정하는 메소드
	@Bean(name="postgresSqlSessionFactory") // 이름 붙여버림 
	public SqlSessionFactory postgresSqlSessionFactory(@Qualifier("postgresDataSource") DataSource datasource)
	throws Exception   // 팩토리로 통로 열고 통로 이름 붙임 postgresDataSource 붙여서 이 프로퍼티 파일 찾기 쉽게 qualifier 씀
	                  // 문장 들어가니까 예외처리하고
	{
		SqlSessionFactoryBean factory=new SqlSessionFactoryBean(); // factory 에서 객체를 가져오면 안 됨 
		                                                         // bean에서 가져와야 함 왜였지?
		
		factory.setDataSource(datasource);  // 팩토리에 데이터 가져온거 넣기
		factory.setTypeAliasesPackage("com.sist.web.vo");
		
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		//   패턴으로 찾기 쉽게 하는 함수 선언함
		
		List<Resource> resources = new ArrayList<>();
		resources.addAll(Arrays.asList(resolver.getResources("classpath*:/mybatis/mapper/*.xml")));
		resources.addAll(Arrays.asList(resolver.getResources("classpath*:/mybatis/mapper/postgres/*.xml")));
		factory.setMapperLocations(resources.toArray(new Resource[0]));
		
		return factory.getObject();
	}
	
	@Bean(name="postgresSessionTemplate")
	public SqlSessionTemplate postgresSessionTemplate(@Qualifier("postgresSqlSessionFactory") SqlSessionFactory sqlSessionFactory)  
	{
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
