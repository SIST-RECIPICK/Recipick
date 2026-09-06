package com.sist.web.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class IngredientUnitVO {
	private int recipe_id;
    private String name;
    private Double amount;
    private String unit;
    private String amount_text;
    private String original;
    
    //재료 카테고리
    private String category_name = new IngredientVO().getCategory_name();
    
    //재료 구매 링크
    private List<ShopLinkVO> shopVO = new ArrayList<ShopLinkVO>();
}
