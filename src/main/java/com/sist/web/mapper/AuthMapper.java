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

	// 닉네임으로 사용자 찾기
	public UsersVO findUserByNickname(@Param("nickname") String nickname);

	// userId로 로컬 계정 조회 (비밀번호 포함, 로그인 검증용)
	public LocalAccountVO findLocalAccountWithPasswordByUserId(@Param("user_id") int user_id);

	// userId로 계정 상태/역할 재조회 (토큰 재발급 시 사용)
	public UsersVO findUserStatusById(@Param("id") int id);

	// users 레코드 생성 (데이터 추가)
	public int insertUser(UsersVO user);

	// local_accounts 레코드 생성 (데이터 추가)
	public int insertLocalAccount(LocalAccountVO localAccount);
}
