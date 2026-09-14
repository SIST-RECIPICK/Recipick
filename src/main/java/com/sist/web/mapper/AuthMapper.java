package com.sist.web.mapper;

import java.time.LocalDateTime;
import java.util.List;

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

	// userId의 로컬 계정 비밀번호 변경 (비밀번호 재설정 시 사용)
	public int updatePassword(@Param("user_id") int user_id, @Param("password") String password);

	// userId 계정 소프트탈퇴 처리 (status = WITHDRAWN, withdrawn_at 기록)
	public int withdrawUser(@Param("id") int id);

	// userId 계정 복구 처리 (status = ACTIVE, withdrawn_at 초기화)
	public int recoverUser(@Param("id") int id);

	// 하드탈퇴 대상 조회 (status = WITHDRAWN, withdrawn_at이 cutoffDate 이전)
	public List<Integer> findHardDeleteCandidates(@Param("cutoffDate") LocalDateTime cutoffDate);

	// 하드탈퇴 - users 익명화 처리 (email/nickname 대체, 개인정보 컬럼 NULL, status = DELETE)
	public int anonymizeUser(@Param("userId") int userId, @Param("email") String email,
			@Param("nickname") String nickname);

	// 하드탈퇴 - local_accounts 행 완전 삭제
	public int deleteLocalAccount(@Param("user_id") int user_id);

	// users 레코드 생성 (데이터 추가)
	public int insertUser(UsersVO user);

	// local_accounts 레코드 생성 (데이터 추가)
	public int insertLocalAccount(LocalAccountVO localAccount);
}
