//package com.pjx.pjxserver.config.Security.filter;
//
//import com.pjx.pjxserver.common.jwt.exception.JwtException.NoAccessTokenException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.AuthenticationEntryPoint;
//import org.springframework.stereotype.Component;
//
//import static com.pjx.pjxserver.common.Token.ACCESS_TOKEN_HEADER;
//
//
//@Slf4j
//@Component
//public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
//
//    @Override
//    public void commence(HttpServletRequest request, HttpServletResponse response,
//                         AuthenticationException authException) {
//        if (request.getHeader(ACCESS_TOKEN_HEADER) == null) {
//            throw new NoAccessTokenException();
//        }
//    }
//}
