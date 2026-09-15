package com.sist.web.vo;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecipeVectorVO {
	private Long id;
	private Long recipe_id;
	private String content;
	private String embedding;
}
