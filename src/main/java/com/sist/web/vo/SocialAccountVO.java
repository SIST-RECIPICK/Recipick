package com.sist.web.vo;

import lombok.Data;

@Data
public class SocialAccountVO {
	private int id;
	private String provider;
	private String provider_id;
	private int users_id;
}