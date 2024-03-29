package com.team.leaf.user.account.oauth;

import com.team.leaf.user.account.dto.common.LoginType;
import com.team.leaf.user.account.dto.request.oauth.OAuth2TokenDto;
import com.team.leaf.user.account.entity.AccountDetail;
import org.springframework.http.ResponseEntity;

public interface OAuth2LoginInfo {

    ResponseEntity<String> requestAccessToken(String code);
    OAuth2TokenDto getAccessToken(ResponseEntity<String> response);

    ResponseEntity<String> requestUserInfo(OAuth2TokenDto oAuth2TokenDto);

    ResponseEntity<String> requestUserInfoForApp(String accessToken);

    AccountDetail getUserInfo(ResponseEntity<String> userInfoRes);


    default LoginType type() {
        if(this instanceof GoogleLoginInfo) {
            return LoginType.GOOGLE;
        } else if (this instanceof KakaoLoginInfo) {
            return LoginType.KAKAO;
        } else if (this instanceof NaverLoginInfo) {
            return LoginType.NAVER;
        } else {
            return LoginType.JWT;
        }
    }

}

