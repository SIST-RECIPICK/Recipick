package com.sist.web.vo;

import java.util.List;

import lombok.Data;

@Data
public class CurationCreateVO {
	private int year;
	private int month;
	private String title;
	private List<CurationDetailVO> details;
}
