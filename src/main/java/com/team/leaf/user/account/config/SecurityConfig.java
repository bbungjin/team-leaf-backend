package com.team.leaf.user.account.config;

import com.team.leaf.user.account.entity.AccountRole;
import com.team.leaf.user.account.jwt.JwtTokenFilter;
import com.team.leaf.user.account.jwt.JwtTokenUtil;
import com.team.leaf.user.account.jwt.PrincipalDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenUtil jwtTokenUtil;
    private final PrincipalDetailsService principalDetailsService;
    private final RedisTemplate redisTemplate;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(principalDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(provider);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity)throws Exception {
        httpSecurity
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((sessionManagement) -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        .requestMatchers("/login/**", "/login/oauth2/code/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**" , "/auto-complete").permitAll()
                        .requestMatchers("/account/**", "/alert/**", "/board/**","/alert/**", "/error/**").permitAll()
                        .requestMatchers("/alert/notify", "/history" , "/history/**", "/account/mypage/**").hasAnyAuthority(AccountRole.USER.name(), AccountRole.SELLER.name())
                        .requestMatchers("/cart/**", "/chat/room/buyer", "/product/{productId}/coupon/download/{couponId}", "/follow", "/product/{productId}/wish-list").hasAuthority(AccountRole.USER.name())
                        .requestMatchers(HttpMethod.GET, "/product/wish").hasAuthority(AccountRole.USER.name())
                        .requestMatchers("/chat/room/seller", "/product/{productId}/coupon", "/seller/notice", "/seller/notice/{noticeId}").hasAuthority(AccountRole.SELLER.name())
                        .requestMatchers("/product/**" , "/seller/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtTokenFilter(jwtTokenUtil, redisTemplate), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

}

