package com.example.gifbottary.common.security.config;

import com.example.gifbottary.common.security.principal.AnonymousPrincipal;
import com.example.gifbottary.domain.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .anonymous(anonymous -> anonymous.principal(new AnonymousPrincipal()))
                .authorizeHttpRequests(auth -> auth
                        // 정적 프론트 페이지
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/*.html",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        // 인증 API 중 공개 API
                        .requestMatchers(
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login",
                                "/ws/**",
                                "/portone-test.html"
                        ).permitAll()

                        // 내 상품 목록 조회는 인증 필요
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/me").authenticated()

                        // 공개 조회 API
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/products",
                                "/api/v1/products/*",
                                "/api/v2/products",
                                "/api/v1/search/products",
                                "/api/v1/search/popular-keywords",
                                "/api/v2/search/popular-keywords"
                        ).permitAll()

                        // PortOne Webhook이 POST라면 공개 필요
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/portone").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/payments/portone").permitAll()

                        // 그 외 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }
}