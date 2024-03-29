package com.team.leaf.user.account.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.leaf.user.account.dto.request.oauth.OAuth2TokenDto;
import com.team.leaf.user.account.entity.AccountDetail;
import com.team.leaf.user.account.exception.AccountException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class NaverLoginInfo implements OAuth2LoginInfo {

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naver_client_id;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naver_client_secret;

    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String naver_redirect_uri;

    @Value("${spring.security.oauth2.client.registration.naver.authorization-grant-type}")
    private String naver_grant_type;

    @Value("${spring.security.oauth2.client.provider.naver.token_uri}")
    private String naver_token_uri;

    @Value("${spring.security.oauth2.client.provider.naver.user-info-uri}")
    private String naver_user_info_uri;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public ResponseEntity<String> requestAccessToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        try {
            params.set("grant_type", naver_grant_type);
            params.set("client_id", naver_client_id);
            params.set("client_secret", naver_client_secret);
            params.set("code", code);
            params.set("state", new BigInteger(130, new SecureRandom()).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(naver_token_uri, params, String.class);

        if(responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity;
        } else {
            throw new AccountException("네이버 로그인에 실패했습니다.");
        }
    }

    @Override
    public OAuth2TokenDto getAccessToken(ResponseEntity<String> response) {
        try {
            log.info("response.getBody() : {}", response.getBody());
            return objectMapper.readValue(response.getBody(), OAuth2TokenDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error while processing JSON: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public ResponseEntity<String> requestUserInfo(OAuth2TokenDto accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Bearer " + accessToken.getAccess_token());

        URI uri = UriComponentsBuilder
                .fromUriString(naver_user_info_uri)
                .build().toUri();

        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    @Override
    public ResponseEntity<String> requestUserInfoForApp(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+ accessToken);

        URI uri = UriComponentsBuilder
                .fromUriString(naver_user_info_uri)
                .build().toUri();

        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    @Override
    public AccountDetail getUserInfo(ResponseEntity<String> userInfoRes) {

        JSONObject jsonObject =
                (JSONObject) JSONValue.parse(Objects.requireNonNull(userInfoRes.getBody()));
        JSONObject accountObject = (JSONObject) jsonObject.get("response");

        return AccountDetail.builder()
                //.oauthId((Long) jsonObject.get("id"))
                .name((String) accountObject.get("name"))
                .email((String) accountObject.get("email"))
                .birthday((String) accountObject.get("birthday"))
                .phone((String) accountObject.get("mobile"))
                .build();
    }

}
