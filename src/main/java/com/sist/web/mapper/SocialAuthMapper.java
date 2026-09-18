package com.sist.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.LocalAccountVO;
import com.sist.web.vo.SocialAccountVO;
import com.sist.web.vo.UsersVO;

@Mapper
@Repository
public interface SocialAuthMapper {
	SocialAccountVO findSocialAccount(@Param("provider") String provider, @Param("provider_id") String provider_id);
	SocialAccountVO findSocialAccountByUserId(@Param("users_id") int users_id);
	int insertSocialAccount(SocialAccountVO socialAccount);
	UsersVO findUserByEmail(@Param("email") String email);
	UsersVO findUserById(@Param("id") int id);
	int insertUser(UsersVO user);
	LocalAccountVO findLocalAccountByEmail(@Param("email") String email);
}