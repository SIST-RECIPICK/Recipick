package com.sist.web.mapper;

import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.UsersVO;

@Mapper
@Repository
public interface MypageMapper {

    public UsersVO mypageSideProfile(@Param("userId") int userId);

    public Map<String, Object> mypageMainCount(@Param("userId") int userId);
}