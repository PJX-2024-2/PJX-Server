package com.pjx.pjxserver.common;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
//
//@Component
//public class JwtUtil {
//
//    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 강력한 시크릿 키 생성
//
//    // 토큰 생성 메소드
//    public String generateToken(Map<String, Object> claims, String subject) {
//        long expirationTime = 1000 * 60 * 60 * 24; // 토큰 만료 시간: 24시간
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(subject)
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
//                .signWith(key)
//                .compact();
//    }
//
//    // 토큰에서 사용자 정보 추출
//    public String extractSubject(String token) {
//        return Jwts.parserBuilder().setSigningKey(key).build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
//
//    // 토큰 유효성 검증
//    public boolean isTokenValid(String token, String userDetails) {
//        final String username = extractSubject(token);
//        return (username.equals(userDetails) && !isTokenExpired(token));
//    }
//
//    private boolean isTokenExpired(String token) {
//        return Jwts.parserBuilder().setSigningKey(key).build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getExpiration()
//                .before(new Date());
//    }
//
//}
@Component
public class JwtUtil {

    private final String SECRET_KEY = "0c535327830605d6497c4189fba2e8c830d9442cb0fac22a78fc1690a4686e4eed3e8cf8f9e795c0d127f39d79b355f5bdf7999e320b9d6e0bc947f2e19ab913";

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 토큰 생성 메소드
    public String generateToken(Map<String, Object> claims, String subject) {
        long expirationTime = 1000L * 60 * 60 * 24 * 365 * 24;// 토큰 만료 시간: 24년

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date()) // 현재 시간
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    // 토큰에서 사용자 정보 추출
    public String extractSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰 유효성 검증
    public boolean isTokenValid(String token, String userDetails) {
        final String username = extractSubject(token);
        return (username.equals(userDetails) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }
}
