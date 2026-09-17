package com.sist.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLinkRequest {
    private String idToken;
    private String password;
}