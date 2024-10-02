package com.pjx.pjxserver.config.Security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pjx.pjxserver.common.exception.ErrorResponse;
import com.pjx.pjxserver.common.jwt.exception.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (JwtException exception) {

            setErrorResponse(response, exception);
        }

    }

    private void setErrorResponse(HttpServletResponse response, JwtException exception) throws IOException {
        log.error("Code : {}, Message : {}", exception.getErrorInfo().getCode(), exception.getErrorInfo().getMessage());
        String errorResponse = objectMapper.writeValueAsString(
                ErrorResponse.of(exception.getStatus(), exception.getErrorInfo()));
        response.setStatus(exception.getStatus().getValue());
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(errorResponse);
    }
}
