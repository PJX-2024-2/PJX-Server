//package com.pjx.pjxserver.config.Security.filter;
//
//import com.ittory.api.auth.dto.MemberDetails;
//import com.ittory.common.jwt.AccessTokenInfo;
//import com.ittory.common.jwt.JwtProvider;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//import static com.ittory.common.constant.TokenConstant.ACCESS_TOKEN_HEADER;
//import static com.pjx.pjxserver.common.Token.ACCESS_TOKEN_HEADER;
//
//@RequiredArgsConstructor
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JwtProvider jwtProvider;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        String accessToken = getTokenByHeader(request);
//
//        if (accessToken != null) {
//            Authentication authentication = getAuthentication(accessToken);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private String getTokenByHeader(HttpServletRequest request) {
//        return request.getHeader(ACCESS_TOKEN_HEADER);
//    }
//
//    private Authentication getAuthentication(String accessToken) {
//        AccessTokenInfo accessTokenInfo = jwtProvider.resolveToken(accessToken);
//        UserDetails userDetails = new MemberDetails(Long.parseLong(accessTokenInfo.getMemberId()),
//                accessTokenInfo.getRole());
//        return new UsernamePasswordAuthenticationToken(userDetails, accessToken, userDetails.getAuthorities());
//    }
//
//}
