package com.sist.web.restcontroller;
import java.io.File;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import com.sist.web.util.FileUploadUtil;
import java.util.*;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.RecipeService;
import com.sist.web.vo.*;


import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
public class RecipeRestController {

	private final RecipeService rService;
	
	// 이미지 업로드 저장 경로 (고정 경로, 공용 설정 파일은 건드리지 않음)
		private static final String UPLOAD_DIR = "C:/upload";


	// 레시피 목록
	@GetMapping("/recipe/list")
	public ResponseEntity<?> recipe_list(@RequestParam("page") int page,
			@RequestParam(value = "sort", required = false, defaultValue = "latest") String sort) {
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, Object> map = new HashMap<>();
		int start = (page - 1) * 12;

		map.put("start", start);
		map.put("sort", sort);

		try {
		    List<RecipeListVO> list = rService.recipeListData(map);
		    int[] pages = rService.pages(page);
		    int totalCount = rService.recipeTotalCount();

		    resultMap.put("list", list);
		    resultMap.put("pages", pages);
		    resultMap.put("totalCount", totalCount);

		} catch (Exception ex) {
		    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(resultMap);
	}

	// 특정카테고리 조회 + 페이징
	@GetMapping("/recipe/category")
	public ResponseEntity<?> recipe_category(@RequestParam("main_category") String main_category,
			@RequestParam("page") int page,
	        @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort)
	{
		Map<String, Object> resultMap = new HashMap<>();
		Map map = new HashMap();
		
		int start = (page - 1) * 12;
		   
		   map.put("main_category", main_category);
		   map.put("page", page);
		   map.put("start", start);
		   map.put("sort", sort);
		
		try {
		    List<RecipeListVO> list = rService.recipeCategoryData(map);
		    int[] pages = rService.category_pages(map);
		    
		    resultMap.put("list", list);
		    resultMap.put("pages", pages);
		    
		} catch (Exception ex) {
		    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(resultMap);
	}
	
	// 좋아요 토글 버튼
	@PostMapping("/recipe/like")
	public ResponseEntity<?> recipe_like(@RequestParam("user_id") int user_id,
	        @RequestParam("recipe_id") int recipe_id)
	{
	    Map<String, Object> map = new HashMap<>();
	    map.put("user_id", user_id);
	    map.put("recipe_id", recipe_id);

	    Map<String, Object> resultMap = new HashMap<>();
	    try {
	        boolean liked = rService.toggleLike(map);
	        resultMap.put("liked", liked);
	    } catch (Exception ex) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	    return ResponseEntity.ok(resultMap);
	}
	
	// 키워드 검색 조회(레시피명 + 해시태그 + 재료정보)
	@GetMapping("/recipe/keyword")
	public ResponseEntity<?> recipe_keyword(@RequestParam("keyword") String keyword,
			@RequestParam("page") int page,
	        @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort)
	{
		Map<String, Object> resultMap = new HashMap<>();
		Map map = new HashMap();

		int start = (page - 1) * 12;

		map.put("keyword", keyword);
		map.put("page", page);
		map.put("start", start);
		map.put("sort", sort);

		try {
			List<RecipeListVO> list = rService.recipeSearchData(map);
			int[] pages = rService.search_pages(map);
            
			int totalCount = rService.searchTotalCount(map);
			resultMap.put("totalCount", totalCount);
			
			resultMap.put("list", list);
			resultMap.put("pages", pages);

		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(resultMap);
	}
	
	// 카테고리 + 키워드 통합 조회
	@GetMapping("/recipe/filter")
	public ResponseEntity<?> recipe_filter(
	        @RequestParam(value = "main_category", required = false) String main_category,
	        @RequestParam(value = "keyword", required = false) String keyword,
	        @RequestParam("page") int page,
	        @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort)
	{
		Map<String, Object> resultMap = new HashMap<>();
		Map map = new HashMap();

		int start = (page - 1) * 12;

		map.put("main_category", main_category);
		map.put("keyword", keyword);
		map.put("page", page);
		map.put("start", start);
		map.put("sort", sort);

		try {
			List<RecipeListVO> list = rService.recipeFilterData(map);
			int[] pages = rService.filter_pages(map);
            
			int totalCount = rService.filterTotalCount(map);
			resultMap.put("totalCount", totalCount);
			
			resultMap.put("list", list);
			resultMap.put("pages", pages);

		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(resultMap);
	}

	// 마이페이지 - 나의 레시피 목록 출력
	@GetMapping("/recipe/mylist")
	public ResponseEntity<?> recipe_mylist(@RequestParam("user_id") int user_id,
			@RequestParam("page") int page)
	{
		Map<String, Object> resultMap = new HashMap<>();
		Map map = new HashMap();

		int start = (page - 1) * 12;

		map.put("user_id", user_id);
		map.put("page", page);
		map.put("start", start);

		try {
			List<RecipeListVO> list = rService.myRecipeListData(map);
			int[] pages = rService.myPages(map);
			int totalCount = rService.myTotalCount(map);

			resultMap.put("list", list);
			resultMap.put("pages", pages);
			resultMap.put("totalCount", totalCount);

		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(resultMap);
	}
	
	// 레시피 등록 (이미지 파일 + 텍스트를 한 번에 받음 -> multipart/form-data)
	@PostMapping("/recipe/insert")
	public ResponseEntity<?> recipe_insert(HttpServletRequest request,
			@RequestParam("user_id") int user_id) throws Exception {

		// 1. 이미지 저장 폴더 경로 (고정 경로 사용)
		String uploadPath = UPLOAD_DIR;

		// 2. 텍스트 필드 하나씩 꺼내기
		RecipeInsertVO vo = new RecipeInsertVO();
		vo.setRcp_nm(request.getParameter("rcp_nm"));
		vo.setRcp_way2(request.getParameter("rcp_way2"));
		vo.setRcp_pat2(request.getParameter("rcp_pat2"));
		vo.setInfo_wgt(request.getParameter("info_wgt"));
		vo.setHash_tag(request.getParameter("hash_tag"));
		vo.setRcp_parts_dtls(request.getParameter("rcp_parts_dtls"));
		vo.setRcp_na_tip(request.getParameter("rcp_na_tip"));
		vo.setUser_id(user_id);

		// 영양정보: 문자열로 들어오므로 숫자로 변환 (비어있으면 0으로 처리)
		vo.setInfo_eng(parseDoubleOrZero(request.getParameter("info_eng")));
		vo.setInfo_car(parseDoubleOrZero(request.getParameter("info_car")));
		vo.setInfo_pro(parseDoubleOrZero(request.getParameter("info_pro")));
		vo.setInfo_fat(parseDoubleOrZero(request.getParameter("info_fat")));
		vo.setInfo_na(parseDoubleOrZero(request.getParameter("info_na")));

		// 3. 대표이미지 저장
		Part mainImagePart = request.getPart("att_file_no_main");
		String mainImageName = FileUploadUtil.upload(uploadPath, mainImagePart);
		vo.setAtt_file_no_main(mainImageName);
		vo.setAtt_file_no_mk(mainImageName);

		// 4. 조리순서 여러 단계 + 단계별 이미지 처리
		int stepCount = Integer.parseInt(request.getParameter("stepCount"));
		List<RecipeManualVO> manualList = new ArrayList<>();
		for (int i = 1; i <= stepCount; i++) {
			RecipeManualVO manual = new RecipeManualVO();
			manual.setStep_no(i);
			manual.setManual_desc(request.getParameter("manual_desc_" + i));

			Part stepImagePart = request.getPart("manual_img_" + i);
			String stepImageName = FileUploadUtil.upload(uploadPath, stepImagePart);
			manual.setManual_img(stepImageName);

			manualList.add(manual);
		}
		vo.setManualList(manualList);

		// 5. 저장
		Map<String, Object> resultMap = new HashMap<>();
		try {
			int newRcpSeq = rService.recipeInsert(vo);
			resultMap.put("rcp_seq", newRcpSeq);
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(resultMap);
	}

	// 문자열을 숫자(double)로 바꾸되, 비어있거나 null이면 0.0 반환
	// 영양정보 입력칸을 선택사항(비워도 됨)으로 두었기 때문에 필요한 안전장치
	private double parseDoubleOrZero(String value) {
		if (value == null || value.trim().isEmpty()) {
			return 0.0;
		}
		return Double.parseDouble(value);
	}
	
	// 레시피 삭제
	// 삭제 대상은 rcp_seq(PK) 하나로 특정
	@PostMapping("/recipe/delete")
	public ResponseEntity<?> recipe_delete(@RequestParam("rcp_seq") int rcp_seq) {

		Map<String, Object> resultMap = new HashMap<>();

		try {
			// Controller는 직접 DB에 접근하지 않고, Service한테 "이 rcp_seq 삭제해줘"라고 시킴
			// Service -> Mapper -> XML의 <delete> 쿼리가 실행되고, 삭제된 row 수(int)가 돌아옴
			// 정상 삭제면 1, 해당 rcp_seq가 없으면 0
			int result = rService.deleteRecipe(rcp_seq);
			resultMap.put("result", result);

		}catch (Exception ex) {
			ex.printStackTrace();  // 콘솔에 에러 강제 출력 
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(resultMap);
	}
	
	// 레시피 수정 (이미지 파일 + 텍스트를 한 번에 받음 -> multipart/form-data)
	// 수정 대상은 rcp_seq(PK)로 특정
	@PostMapping("/recipe/update")
	public ResponseEntity<?> recipe_update(HttpServletRequest request,
			@RequestParam("rcp_seq") int rcp_seq) throws Exception {

		String uploadPath = UPLOAD_DIR;

		// 1. 텍스트 필드 하나씩 꺼내기 (등록 때와 동일한 패턴)
		RecipeInsertVO vo = new RecipeInsertVO();
		vo.setRcp_seq(rcp_seq); // ★ 수정 대상 지정 (등록에는 없던 부분)
		vo.setRcp_nm(request.getParameter("rcp_nm"));
		vo.setRcp_way2(request.getParameter("rcp_way2"));
		vo.setRcp_pat2(request.getParameter("rcp_pat2"));
		vo.setInfo_wgt(request.getParameter("info_wgt"));
		vo.setHash_tag(request.getParameter("hash_tag"));
		vo.setRcp_parts_dtls(request.getParameter("rcp_parts_dtls"));
		vo.setRcp_na_tip(request.getParameter("rcp_na_tip"));

		vo.setInfo_eng(parseDoubleOrZero(request.getParameter("info_eng")));
		vo.setInfo_car(parseDoubleOrZero(request.getParameter("info_car")));
		vo.setInfo_pro(parseDoubleOrZero(request.getParameter("info_pro")));
		vo.setInfo_fat(parseDoubleOrZero(request.getParameter("info_fat")));
		vo.setInfo_na(parseDoubleOrZero(request.getParameter("info_na")));

		// 2. 대표이미지: 새로 올렸을 때만 처리 (안 올렸으면 vo의 값은 null로 남아있음
		//    -> Mapper XML의 <if test="att_file_no_main != null">가 기존 이미지 유지시킴)
		Part mainImagePart = request.getPart("att_file_no_main");
		if (mainImagePart != null && mainImagePart.getSize() > 0) {
			String mainImageName = FileUploadUtil.upload(uploadPath, mainImagePart);
			vo.setAtt_file_no_main(mainImageName);
		}

		// 3. 조리순서: 등록 때와 동일하게 전체를 다시 받음
		//    (기존 것 지우고 재삽입하는 방식이라, 수정 화면에서도 전체 단계를 다시 보내줘야 함)
		int stepCount = Integer.parseInt(request.getParameter("stepCount"));
		List<RecipeManualVO> manualList = new ArrayList<>();
		for (int i = 1; i <= stepCount; i++) {
			RecipeManualVO manual = new RecipeManualVO();
			manual.setStep_no(i);
			manual.setManual_desc(request.getParameter("manual_desc_" + i));

			Part stepImagePart = request.getPart("manual_img_" + i);
			if (stepImagePart != null && stepImagePart.getSize() > 0) {
				manual.setManual_img(FileUploadUtil.upload(uploadPath, stepImagePart));
			}
			else {
			    // 새 이미지 없으면 빈 문자열로 저장 (null 대신)
			    manual.setManual_img("");
			}
			manualList.add(manual);
		}
		vo.setManualList(manualList);

		// 4. 수정 실행
		Map<String, Object> resultMap = new HashMap<>();
		try {
			rService.recipeUpdate(vo);
			resultMap.put("rcp_seq", rcp_seq);
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(resultMap);
	}
}