package com.sist.web.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
		
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		//   패턴으로 찾기 쉽게 하는 함수 선언함
		
		factory.setMapperLocations(resolver.getResources("classpath*:/mapper/postgres/*.xml")); 
		// 리졸버가 가져온 xml 데이터들을 팩토리에 집어 넣음?
		return factory.getObject(); // 팩토리만 쓰면 안 됨 데이터들을 빈객체에 넣어놨기 때문에?
	}
	
	@Bean(name="postgresSessionTemplate")
	public SqlSessionTemplate postgresSessionTemplate(@Qualifier("postgresSqlSessionFactory") SqlSessionFactory sqlSessionFactory)  
	{
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
