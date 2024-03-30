package com.team.leaf.user.account.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.leaf.user.account.dto.request.oauth.OAuth2TokenDto;
import com.team.leaf.user.account.entity.AccountDetail;
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

import java.net.URI;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class KakaoLoginInfo implements OAuth2LoginInfo {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakao_client_id;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakao_redirect_uri;

    @Value("${spring.security.oauth2.client.registration.kakao.authorization-grant-type}")
    private String kakao_grant_type;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String kakao_token_uri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String kakao_user_info_uri;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public ResponseEntity<String> requestAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set("code", code);
        params.set("client_id", kakao_client_id);
        params.set("redirect_uri", kakao_redirect_uri);
        params.set("grant_type", kakao_grant_type);

        URI uri = UriComponentsBuilder
                .fromUriString(kakao_token_uri)
                .queryParams(params)
                .encode().build().toUri();
        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
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
        headers.add("Authorization","Bearer "+ accessToken.getAccess_token());

        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        URI uri = UriComponentsBuilder
                .fromUriString(kakao_user_info_uri)
                .encode().build().toUri();

        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    @Override
    public ResponseEntity<String> requestUserInfoForApp(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+ accessToken);

        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        URI uri = UriComponentsBuilder
                .fromUriString(kakao_user_info_uri)
                .encode().build().toUri();

        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    @Override
    public AccountDetail getUserInfo(ResponseEntity<String> userInfoRes) {

        JSONObject jsonObject =
                (JSONObject) JSONValue.parse(Objects.requireNonNull(userInfoRes.getBody()));
        JSONObject accountObject = (JSONObject) jsonObject.get("kakao_account");

        return AccountDetail.builder()
                //.oauthId((Long) jsonObject.get("id"))
                .name((String) accountObject.get("name"))
                .email((String) accountObject.get("email"))
                .birthday((String) accountObject.get("birthday"))
                .phone((String) accountObject.get("phone_number"))
                .build();
    }

}

