package com.pjx.pjxserver.common.jwt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessTokenInfo {
    private final String memberId;
    private final String role;

    public static AccessTokenInfo of(String memberId, String role) {
        return new AccessTokenInfo(memberId, role);
    }
}
