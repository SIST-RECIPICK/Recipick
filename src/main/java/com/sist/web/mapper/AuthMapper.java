package com.sist.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.LocalAccountVO;
import com.sist.web.vo.UsersVO;

@Mapper
@Repository
public interface AuthMapper {

	// 이메일로 사용자 찾기
	public UsersVO findUserByEmail(@Param("email") String email);

	// userId로 로컬 가입 사용자 찾기
	public LocalAccountVO findLocalAccountByUserId(@Param("user_id") int user_id);
}
