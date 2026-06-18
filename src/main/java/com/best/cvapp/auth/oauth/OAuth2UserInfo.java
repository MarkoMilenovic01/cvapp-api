package com.best.cvapp.auth.oauth;


import org.springframework.security.oauth2.core.user.OAuth2User;

public record OAuth2UserInfo(String email, String name) {
    public static OAuth2UserInfo from(OAuth2User oAuth2User) {
        return new OAuth2UserInfo(
                oAuth2User.getAttribute("email"),
                oAuth2User.getAttribute("name")
        );
    }
}