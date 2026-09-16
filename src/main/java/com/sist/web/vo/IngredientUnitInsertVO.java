package com.sist.web.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
// 레시피 등록 시 재료 담기 위한 그릇
@Data
public class IngredientUnitInsertVO {

	private int recipe_id; // 레시피 번호
    private String name; // 재료명
    private Double amount; // 재료양
    private String unit; // 재료단위
    private String amount_text; // 대체단위
    private String original; // 가공 전 텍스트
    
   
}
