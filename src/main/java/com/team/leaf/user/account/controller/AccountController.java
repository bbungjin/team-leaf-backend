package com.team.leaf.user.account.controller;

import com.team.leaf.user.account.dto.common.DuplicateEmailRequest;
import com.team.leaf.user.account.dto.common.DuplicatePhoneRequest;
import com.team.leaf.user.account.dto.request.jwt.*;
import com.team.leaf.user.account.dto.common.LoginType;
import com.team.leaf.user.account.dto.request.oauth.OAuthLoginRequest;
import com.team.leaf.user.account.dto.response.LoginAccountDto;
import com.team.leaf.user.account.dto.response.OAuth2LoginResponse;
import com.team.leaf.user.account.dto.response.TokenDto;
import com.team.leaf.user.account.exception.ApiResponse;
import com.team.leaf.user.account.jwt.JwtTokenFilter;
import com.team.leaf.user.account.jwt.JwtTokenUtil;
import com.team.leaf.user.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/join")
    @Operation(summary = "자체 로그인 회원가입 API")
    public ApiResponse<String> join(@RequestBody JwtJoinRequest joinRequest) throws IOException {

        return new ApiResponse<>(accountService.join(joinRequest));
    }

    @PostMapping("/join/additional-info")
    @Operation(summary = "[웹 전용] 회원가입 추가 정보 입력 API")
    public ApiResponse<String> joinWithAdditionalInfo(@RequestBody AdditionalJoinInfoRequest request) throws IOException {
        String responseMessage = accountService.joinWithAdditionalInfo(request);
        return new ApiResponse<>(responseMessage);
    }

    @PostMapping("/login")
    @Operation(summary = "자체 로그인 로그인 API")
    public ApiResponse<LoginAccountDto> login(@RequestBody @Valid JwtLoginRequest loginRequest, HttpServletResponse response) {
        return new ApiResponse<>(accountService.login(loginRequest, response));
    }

    @PostMapping("/oauth2/app/login")
    @Operation(summary = "[앱 전용] 소셜 로그인 로그인 API")
    public OAuth2LoginResponse login(@RequestParam Platform platform, @RequestParam(name = "type") LoginType type, @RequestParam(name = "accessToken") String accessToken, HttpServletResponse response) {
        return accountService.oAuth2AppLogin(platform, type, accessToken, response);
    }

    @PostMapping("/oauth2/web/login")
    @Operation(summary = "[웹 전용] 소셜 로그인 로그인 API")
    public OAuth2LoginResponse login(@RequestBody OAuthLoginRequest request, HttpServletResponse response) {
        return accountService.oAuth2WebLogin(request, response);
    }

    @DeleteMapping("/logout")
    @Operation(summary = "자체 로그인 로그아웃 API")
    public ApiResponse<String> logout(@RequestHeader(name = JwtTokenUtil.ACCESS_TOKEN) String accessToken,
                                      @RequestHeader(name = JwtTokenUtil.REFRESH_TOKEN) String refreshToken) {
        return new ApiResponse<>(accountService.logout(accessToken, refreshToken));
    }

    @PostMapping("/issue/token")
    @Operation(summary = "Access Token 갱신 API")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response,
                                                @RequestHeader(name = JwtTokenUtil.REFRESH_TOKEN, required = false) String refreshToken,
                                                @RequestBody PlatformRequest platformRequest) {
        TokenDto newTokenDto = null;

        if (refreshToken == null) {
            String cookie_refreshToken = JwtTokenFilter.getTokenByRequest(request, "refreshToken");

            newTokenDto = accountService.refreshAccessToken(platformRequest.getPlatform(), cookie_refreshToken);
        } else {
            newTokenDto = accountService.refreshAccessToken(platformRequest.getPlatform(), refreshToken);
        }

        accountService.setHeader(response, newTokenDto);

        return new ResponseEntity<>(newTokenDto, HttpStatus.OK);
    }

    @PostMapping("/email")
    @Operation(summary = "중복 이메일 확인 API")
    public ResponseEntity<String> findAccountById(@RequestBody DuplicateEmailRequest emailRequest) {
        String existingUserMessage = accountService.checkEmailDuplicate(emailRequest.getEmail());

        if (!"중복된 데이터가 없습니다.".equals(existingUserMessage)) {
            return ResponseEntity.badRequest().body(existingUserMessage);
        }

        return ResponseEntity.ok("중복된 데이터가 없습니다.");
    }

    @PostMapping("/check/phone")
    @Operation(summary = "중복 회원 확인 API")
    public ResponseEntity<String> checkPhoneDuplicate(@RequestBody DuplicatePhoneRequest phoneRequest) {
        String existingUserMessage = accountService.checkPhoneNumberDuplicate(phoneRequest.getPhone());

        if (!"중복된 데이터가 없습니다.".equals(existingUserMessage)) {
            return ResponseEntity.badRequest().body(existingUserMessage);
        }

        return ResponseEntity.ok("중복된 데이터가 없습니다.");
    }

}
