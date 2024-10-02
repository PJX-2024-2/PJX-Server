//package com.pjx.pjxserver.config.Security;
//
//import com.pjx.pjxserver.common.jwt.JwtProvider;
//import com.pjx.pjxserver.config.Security.filter.CustomAuthenticationEntryPoint;
//import com.pjx.pjxserver.config.Security.filter.ExceptionHandlerFilter;
//import com.pjx.pjxserver.config.Security.filter.JwtAuthenticationFilter;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JwtProvider jwtProvider;
//    private final ExceptionHandlerFilter exceptionHandlerFilter;
//    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .cors(AbstractHttpConfigurer::disable)
//                .csrf(AbstractHttpConfigurer::disable)
//                .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
//                .httpBasic(AbstractHttpConfigurer::disable)
//                .formLogin(AbstractHttpConfigurer::disable)
//                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests((authorize) -> authorize
//                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
//                        .requestMatchers("/api/auth/login/**", "/api/auth/refresh").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .exceptionHandling((config) -> config.authenticationEntryPoint(customAuthenticationEntryPoint))
//                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider),
//                        UsernamePasswordAuthenticationFilter.class)
//                .addFilterBefore(exceptionHandlerFilter, JwtAuthenticationFilter.class);
//
//        return http.build();
//    }
//}
