package com.sist.web.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sist.web.vo.ShopLinkVO;
/*
https://search.shopping.naver.com/ns/search?query=감자  												--- 네이버
https://www.coupang.com/np/search?component=&q=감자 													--- 쿠팡
https://www.lotteon.com/csearch/search/search?render=search&q=감자 									--- 롯데온
https://www.gmarket.co.kr/n/search?spm=gmktpc.home.searchtop.dsearchbox.1fbf486a0O55VD&keyword=감자 	--- G마켓
 */
@Service
public class ShopLinkServiceImpl implements ShopLinkService{

	@Override
	public List<ShopLinkVO> shopLinkData(String name) {
		name = URLEncoder.encode(name, StandardCharsets.UTF_8);
		List<ShopLinkVO> list = new ArrayList<ShopLinkVO>();
		String[] shop = {"네이버","쿠팡","롯데온","지마켓"};
		String[] site = {"https://search.shopping.naver.com/ns/search?query="+name,
						"https://www.coupang.com/np/search?component=&q="+name+"&sorter=salePriceAsc",
						"https://www.lotteon.com/csearch/search/search?render=search&q="+name,
						"https://www.gmarket.co.kr/n/search?spm=gmktpc.home.searchtop.dsearchbox.1fbf486a0O55VD&keyword="+name};
		
		for(int i = 0 ; i < shop.length ; i++)
		{
			ShopLinkVO vo = new ShopLinkVO();
			vo.setShop_name(shop[i]);
			vo.setLink(site[i]);
			list.add(vo);
		}
			
		return list;
	}

}
