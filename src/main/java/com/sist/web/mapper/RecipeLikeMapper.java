package com.sist.web.mapper;

import java.util.List;
import java.util.Map;

public interface RecipeLikeMapper {
	 List<Map<String, Object>> countLikesByRecipe();
}
