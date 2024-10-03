//package com.pjx.pjxserver.common.jwt;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.pjx.pjxserver.common.jwt.exception.JwtException;
//import com.pjx.pjxserver.common.jwt.exception.JwtException.InvalidateTokenException;
//import com.pjx.pjxserver.common.jwt.exception.JwtException.UnSupportedTokenException;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.ExpiredJwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.nio.charset.StandardCharsets;
//import java.security.Key;
//import java.util.Base64;
//import java.util.Date;
//
//import static com.pjx.pjxserver.common.Token.TOKEN_TYPER;
//
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class JwtProvider {
//
//    @Value("${spring.jwt.secret}")
//    private String SECRET_KEY;
//
//    @Value("${spring.jwt.token.access-expiration-time}")
//    private Long ACCESS_TOKEN_EXPIRATION_TIME;
//
//    @Value("${spring.jwt.token.refresh-expiration-time}")
//    private Long REFRESH_TOKEN_EXPIRATION_TIME;
//
//    @PostConstruct
//    protected void init() {
//        SECRET_KEY = Base64.getEncoder().encodeToString(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
//    }
//
//    private Key getSecretKey() {
//        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
//    }
//
//    public String createAccessToken(Long memberId, String role) {
//        Claims claims = Jwts.claims().setSubject(memberId.toString());
//        Date now = new Date();
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setIssuedAt(now)
//                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME * 1000))
//                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
//                .claim("role", role)
//                .claim("type", "ACCESS")
//                .compact();
//    }
//
//    public String createRefreshToken(Long memberId) {
//        Claims claims = Jwts.claims().setSubject(memberId.toString());
//        Date now = new Date();
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setIssuedAt(now)
//                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_TIME * 1000))
//                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
//                .claim("type", "REFRESH")
//                .compact();
//    }
//
//    public AccessTokenInfo resolveToken(String token) {
//        String claimsJws = getClaimsJws(token);
//        try {
//            Claims body = Jwts.parserBuilder()
//                    .setSigningKey(SECRET_KEY.getBytes()).build()
//                    .parseClaimsJws(claimsJws)
//                    .getBody();
//            return AccessTokenInfo.of(body.getSubject(), (String) body.get("role"));
//        } catch (ExpiredJwtException exception) {
//            throw new JwtException.ExpiredTokenException(token);
//        } catch (Exception exception) {
//            throw new InvalidateTokenException(token);
//        }
//
//    }
//
//    private String getClaimsJws(String token) {
//        String[] splitToken = token.split(TOKEN_TYPER + " ");
//        if (!token.startsWith(TOKEN_TYPER + " ") && splitToken.length != 2) {
//            throw new UnSupportedTokenException(token);
//        }
//        return splitToken[1];
//    }
//
//    public Long getSubByExpiredToken(String token) {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        try {
//            String[] splitToken = token.split("\\.");
//            String base64EncodedBody = splitToken[1];
//            String body = new String(Base64.getUrlDecoder().decode(base64EncodedBody));
//            JsonNode payloadJson = objectMapper.readTree(body);
//            return Long.parseLong(payloadJson.get("sub").asText());
//        } catch (Exception exception) {
//            throw new InvalidateTokenException(token);
//        }
//    }
//
//}
