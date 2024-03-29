package com.team.leaf.user.account.service;

import com.team.leaf.user.account.config.SecurityConfig;
import com.team.leaf.user.account.dto.request.jwt.*;
import com.team.leaf.user.account.dto.common.LoginType;
import com.team.leaf.user.account.dto.request.oauth.OAuth2TokenDto;
import com.team.leaf.user.account.dto.request.oauth.OAuthLoginRequest;
import com.team.leaf.user.account.dto.response.LoginAccountDto;
import com.team.leaf.user.account.dto.response.OAuth2LoginResponse;
import com.team.leaf.user.account.dto.response.TokenDto;
import com.team.leaf.user.account.entity.*;
import com.team.leaf.user.account.exception.AccountException;
import com.team.leaf.user.account.jwt.JwtTokenUtil;
import com.team.leaf.user.account.oauth.OAuth2LoginInfo;
import com.team.leaf.user.account.repository.AccountRepository;
import com.team.leaf.user.account.repository.InterestCategoryRepository;
import com.team.leaf.user.account.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.team.leaf.user.account.jwt.JwtTokenUtil.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final InterestCategoryRepository interestCategoryRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final SecurityConfig jwtSecurityConfig;
    private final RedisTemplate redisTemplate;
    private final List<OAuth2LoginInfo> oAuth2LoginInfoList;

    public void validatePassword(String password) {
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{9,22}$";
        Pattern pwPattern = Pattern.compile(passwordRegex);
        Matcher pwMatcher = pwPattern.matcher(password);

        if (!pwMatcher.matches()) {
            throw new RuntimeException("비밀번호는 최소 9~20자로 구성되어야 하며, 숫자, 영어 대문자, 영어 소문자, 특수 문자를 모두 포함해야 합니다.");
        }
    }

    @Transactional
    public String join(JwtJoinRequest request) {

        validateEmail(request.getEmail());
        validatePassword(request.getPassword());
        validatePhone(request.getPhone());

        if(!request.getPassword().equals(request.getPasswordCheck())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String existingPhone = checkPhoneNumberDuplicate(request.getPhone());
        if (!"중복된 데이터가 없습니다.".equals(existingPhone)) {
            throw new RuntimeException(existingPhone);
        }

        String existingEmail = checkEmailDuplicate(request.getEmail());
        if (!"중복된 데이터가 없습니다.".equals(existingEmail)) {
            throw new RuntimeException(existingEmail);
        }

        // 닉네임 중복 체크
        String nickName = createNickName(request.getEmail());

        AccountDetail accountDetail = AccountDetail.joinAccount(request.getEmail(),jwtSecurityConfig.passwordEncoder().encode(request.getPassword()), request.getPhone(), nickName);
        accountRepository.save(accountDetail);
        return "Success Join";
    }

    private String createNickName(String email) {
        while(true) {
            String random = generateRandomNumber(7);

            String randomNickName = email.substring(0 , 4) + random;
            if(!accountRepository.existsByNickname(randomNickName)) {
                return randomNickName;
            }
        }
    }

    private String generateRandomNumber(int N) {
        String result = "";
        for(int i = 0; i < N; i++) {
            result += Integer.toString((int) ((Math.random()*10000)%10));
        }

        return result;
    }

    public String joinWithAdditionalInfo(AdditionalJoinInfoRequest request) {

        AccountDetail accountDetail = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        accountDetail.updateAdditionalInfo(request.getName(), request.getBirthday(), request.getGender(), request.getUniversityName());

        List<String> selectedCategories = request.getInterestCategories();

        for(String selectedCategory : selectedCategories) {
            InterestCategory interestCategory = new InterestCategory();
            interestCategory.setCategory(selectedCategory);

            interestCategory = interestCategoryRepository.save(interestCategory);

            AccountInterest accountInterest = new AccountInterest(accountDetail, interestCategory);
            accountDetail.getAccountInterests().add(accountInterest);
        }

        accountRepository.save(accountDetail);

        return "Success";

    }

    @Transactional
    public LoginAccountDto login(JwtLoginRequest request, HttpServletResponse response) {
        // 이메일로 유저 정보 확인
        AccountDetail accountDetail = accountRepository.findByEmail(request.getEmail()).orElseThrow(() ->
                new RuntimeException("사용자를 찾을 수 없습니다."));

        // 비밀번호 일치 확인
        if(!jwtSecurityConfig.passwordEncoder().matches(request.getPassword(), accountDetail.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        else {
            TokenDto tokenDto = jwtTokenUtil.createToken(request.getPlatform(), request.getEmail());

            redisTemplate.opsForValue().set("RT:" + request.getEmail(), tokenDto.getRefreshToken(), tokenDto.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

            setHeader(response, tokenDto);

            String access_token = tokenDto.getAccessToken();
            String refresh_token = tokenDto.getRefreshToken();

            return new LoginAccountDto(accountDetail.getEmail(),access_token,refresh_token);
        }
    }

    private OAuth2LoginInfo findOAuth2LoginType(LoginType type) {
        return oAuth2LoginInfoList.stream()
                .filter(x -> x.type() == type)
                .findFirst()
                .orElseThrow(() -> new AccountException("알 수 없는 로그인 타입입니다."));
    }

    public OAuth2LoginResponse oAuth2AppLogin(Platform platform, LoginType type, String accessToken, HttpServletResponse response) {
        OAuth2LoginInfo oAuth2LoginInfo = findOAuth2LoginType(type);

        ResponseEntity<String> userInfoRes = oAuth2LoginInfo.requestUserInfoForApp(accessToken);

        AccountDetail accountDetail = oAuth2LoginInfo.getUserInfo(userInfoRes);

        AccountDetail existOwner = accountRepository.findByEmail(accountDetail.getEmail()).orElse(null);

        if(existOwner == null) {
            accountDetail.setRole(AccountRole.USER);
            accountDetail.setLoginType(type);
            accountRepository.save(accountDetail);
        }

        TokenDto tokenDto = jwtTokenUtil.createToken(platform, accountDetail.getEmail());

        redisTemplate.opsForValue().set("RT:" + accountDetail.getEmail(), tokenDto.getRefreshToken(), tokenDto.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        setHeader(response, tokenDto);

        return new OAuth2LoginResponse(true, existOwner == null ? accountDetail.getUserId() : existOwner.getUserId(), accountDetail.getEmail(), accountDetail.getName(), accountDetail.getBirthday(), accountDetail.getPhone(), tokenDto.getAccessToken(), tokenDto.getRefreshToken());
    }

    public OAuth2LoginResponse oAuth2WebLogin(OAuthLoginRequest request, HttpServletResponse response) {
        OAuth2LoginInfo oAuth2LoginInfo = findOAuth2LoginType(request.getType());
        ResponseEntity<String> accessTokenRes = oAuth2LoginInfo.requestAccessToken(request.getCode());

        OAuth2TokenDto oAuth2Token = oAuth2LoginInfo.getAccessToken(accessTokenRes);

        ResponseEntity<String> userInfoRes = oAuth2LoginInfo.requestUserInfo(oAuth2Token);

        AccountDetail accountDetail = oAuth2LoginInfo.getUserInfo(userInfoRes);
        accountDetail.setLoginType(request.getType());

        AccountDetail existOwner = accountRepository.findByEmail(accountDetail.getEmail()).orElse(null);

        if(existOwner == null) {
            accountDetail.setRole(AccountRole.USER);
            accountDetail.setLoginType(request.getType());
            accountRepository.save(accountDetail);
        }

        TokenDto tokenDto = jwtTokenUtil.createToken(request.getPlatform(), accountDetail.getEmail());

        redisTemplate.opsForValue().set("RT:" + accountDetail.getEmail(), tokenDto.getRefreshToken(), tokenDto.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        setHeader(response, tokenDto);

        return new OAuth2LoginResponse(true, existOwner == null ? accountDetail.getUserId() : existOwner.getUserId(), accountDetail.getEmail(), accountDetail.getName(), accountDetail.getBirthday(), accountDetail.getPhone(), tokenDto.getAccessToken(), tokenDto.getRefreshToken());
    }



    @Transactional
    public String logout(String accessToken, String refreshToken) {
        if(!jwtTokenUtil.tokenValidation(accessToken)) {
            throw new IllegalArgumentException("Invalid Access Token");
        }

        String email = jwtTokenUtil.getEmailFromToken(accessToken);

        if (redisTemplate.opsForValue().get("RT:" + email) != null) {
            redisTemplate.delete("RT:" + email);
        }

        Long expiration = jwtTokenUtil.getExpiration(accessToken);
        refreshTokenRepository.deleteByRefreshToken(refreshToken);

        redisTemplate.opsForValue().set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);

        return "Success Logout";
    }

    @Transactional
    public TokenDto refreshAccessToken(Platform platform, String refreshToken) {
        if (!jwtTokenUtil.refreshTokenValidation(refreshToken, platform)) {
            throw new RuntimeException("Invalid Refresh Token");
        }

        String email = jwtTokenUtil.getEmailFromToken(refreshToken);
        if(!refreshToken.equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token 정보가 일치하지 않습니다");
        }

        String new_accessToken = jwtTokenUtil.recreateAccessToken(refreshToken);

        redisTemplate.opsForValue().set("RT:" + email, new_accessToken, ACCESS_TIME , TimeUnit.MILLISECONDS);

        return TokenDto.builder().accessToken(new_accessToken).build();
    }

    public void validateEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern emailPattern = Pattern.compile(emailRegex);
        Matcher emailMatcher = emailPattern.matcher(email);

        if (!emailMatcher.matches()) {
            throw new RuntimeException("이메일 형식이 올바르지 않습니다.");
        }
    }

    public void validatePhone(String phone) {
        String phoneRegex = "^[0-9]{10,11}$";
        Pattern phonePattern = Pattern.compile(phoneRegex);
        Matcher phoneMatcher = phonePattern.matcher(phone);

        if(!phoneMatcher.matches()) {
            throw new RuntimeException("전화번호 형식이 올바르지 않습니다.");
        }
    }

    /*public String checkPhoneNumberDuplicate(String phone) {
        AccountDetail accountDetail = accountRepository.findByPhone(phone);

        if (accountDetail.getLoginType().equals(LoginType.JWT)) {
            return "일반 로그인으로 가입된 계정이 존재합니다.";
        } else if(accountDetail.getLoginType().equals(LoginType.KAKAO) ||
                accountDetail.getLoginType().equals(LoginType.NAVER) ||
                accountDetail.getLoginType().equals(LoginType.GOOGLE)){
            return accountDetail.getLoginType() + "로그인으로 가입된 계정이 존재합니다.";
        }

        return "중복된 데이터가 없습니다.";
    }*/

    public String checkPhoneNumberDuplicate(String phone) {
        Optional<AccountDetail> optionalAccountDetail = accountRepository.findByPhone(phone);

        if (optionalAccountDetail.isPresent()) {
            AccountDetail accountDetail = optionalAccountDetail.get();
            if (accountDetail.getLoginType().equals(LoginType.JWT)) {
                return "일반 로그인으로 가입된 계정이 존재합니다.";
            } else if(accountDetail.getLoginType().equals(LoginType.KAKAO) ||
                    accountDetail.getLoginType().equals(LoginType.NAVER) ||
                    accountDetail.getLoginType().equals(LoginType.GOOGLE)){
                return accountDetail.getLoginType() + "로그인으로 가입된 계정이 존재합니다.";
            }
        }

        return "중복된 데이터가 없습니다.";
    }

    public String checkEmailDuplicate(String email) {
        Optional<AccountDetail> optionalAccountDetail = accountRepository.findByEmail(email);

        if (optionalAccountDetail.isPresent()) {
            AccountDetail accountDetail = optionalAccountDetail.get();
            if (accountDetail.getLoginType().equals(LoginType.JWT)) {
                return "일반 로그인으로 가입된 계정이 존재합니다.";
            } else if(accountDetail.getLoginType().equals(LoginType.KAKAO) ||
                    accountDetail.getLoginType().equals(LoginType.NAVER) ||
                    accountDetail.getLoginType().equals(LoginType.GOOGLE)){
                return accountDetail.getLoginType() + "로그인으로 가입된 계정이 존재합니다.";
            }
        }

        return "중복된 데이터가 없습니다.";
    }

    public void setHeader(HttpServletResponse response, TokenDto tokenDto) {
        if(tokenDto.getRefreshToken() != null) {
            response.addHeader(JwtTokenUtil.REFRESH_TOKEN, tokenDto.getRefreshToken());
            response.addHeader("Set-Cookie", createRefreshToken(tokenDto.getRefreshToken()).toString());
        }

        if(tokenDto.getAccessToken() != null) {
            response.addHeader(JwtTokenUtil.ACCESS_TOKEN, tokenDto.getAccessToken());
            response.addHeader("Set-Cookie", createAccessToken(tokenDto.getAccessToken()).toString());
        }
    }

    public static ResponseCookie createAccessToken(String access) {
        return ResponseCookie.from("accessToken" , access)
                .path("/")
                .maxAge(30 * 60 * 1000)
                //.secure(true)
                //.domain()
                .httpOnly(true)
                //.sameSite("none")
                .build();
    }

    public static ResponseCookie createRefreshToken(String refresh) {
        return ResponseCookie.from("refreshToken" , refresh)
                .path("/")
                .maxAge(14 * 24 * 60 * 60 * 1000)
                //.secure(true)
                //.domain()
                .httpOnly(true)
                //.sameSite("none")
                .build();
    }


}
