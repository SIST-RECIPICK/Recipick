package com.sist.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.*;
import com.sist.web.vo.IngredientVO;
@Mapper
@Repository
public interface IngredientMapper {
	
       List<IngredientVO> searchData(String keyword); 
}
