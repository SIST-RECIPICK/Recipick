package com.sist.web.service;

import java.util.Map;
import com.sist.web.vo.UsersVO;

public interface MypageService {

    public UsersVO mypageSideProfile(int userId);    
    
    public Map<String, Object> mypageMainCount(int userId);
}