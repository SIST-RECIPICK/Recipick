package com.sist.web.postgres;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
// 임베딩 기반으로 유사한 레시피 찾아주는 기능매퍼
@Mapper
public interface PostgresRecipeMapper {   
	public List<Map<String,Object>> findSimilarRecipes(
	@Param("embedding") String embedding,
	@Param("limit") int limit);
}
