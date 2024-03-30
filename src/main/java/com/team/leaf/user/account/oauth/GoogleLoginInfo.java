package com.team.leaf.user.account.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.leaf.user.account.dto.request.oauth.OAuth2TokenDto;
import com.team.leaf.user.account.entity.AccountDetail;
import com.team.leaf.user.account.exception.AccountException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class GoogleLoginInfo implements OAuth2LoginInfo {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String google_client_id;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String google_client_secret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String google_redirect_uri;

    private String google_token_uri = "https://oauth2.googleapis.com/token";

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public ResponseEntity<String> requestAccessToken(String code) {
        String decode = URLDecoder.decode(code, StandardCharsets.UTF_8);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.set("code", decode);
        queryParams.set("client_id", google_client_id);
        queryParams.set("redirect_uri", google_redirect_uri);
        queryParams.set("client_secret", google_client_secret);
        queryParams.set("grant_type", "authorization_code");

        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(google_token_uri, queryParams, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity;
        } else {
            throw new AccountException("구글 로그인에 실패하였습니다.");
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
        headers.add("Authorization", "Bearer "+ accessToken.getAccess_token());

        URI uri = UriComponentsBuilder
                .fromUriString("https://www.googleapis.com/oauth2/v1/userinfo")
                .build().toUri();

        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    @Override
    public ResponseEntity<String> requestUserInfoForApp(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+ accessToken);

        URI uri = UriComponentsBuilder
                .fromUriString("https://www.googleapis.com/oauth2/v1/userinfo")
                .build().toUri();

        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    @Override
    public AccountDetail getUserInfo(ResponseEntity<String> userInfoRes) {
        try {
            return objectMapper.readValue(userInfoRes.getBody(), AccountDetail.class);
        } catch (JsonProcessingException e) {
            log.error("Error while processing JSON: {}", e.getMessage());
            return null;
        }
    }

}
