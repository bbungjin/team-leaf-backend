package com.team.leaf.common.custom;

import com.team.leaf.user.account.entity.AccountDetail;
import com.team.leaf.user.account.jwt.JwtTokenUtil;
import com.team.leaf.user.account.repository.AccountRepository;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CustomLogInArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenUtil jwtTokenUtil;
    private final AccountRepository accountRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(LogIn.class);
        boolean hasUserType = AccountDetail.class.isAssignableFrom(parameter.getParameterType());

        return hasLoginAnnotation && hasUserType;
    }

    @Override
    public AccountDetail resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String accessToken = getAccessTokenFromHeader(request);

        String email = jwtTokenUtil.getEmailFromToken(accessToken);
        AccountDetail account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("not fount User"));

        return account;
    }

    public String getAccessTokenFromHeader(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

}
