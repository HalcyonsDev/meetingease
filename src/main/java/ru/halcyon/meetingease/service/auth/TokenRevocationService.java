package ru.halcyon.meetingease.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.halcyon.meetingease.security.JwtProvider;
import ru.halcyon.meetingease.util.CacheManager;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {
    private final JwtProvider jwtProvider;
    private final CacheManager cacheManager;
    private final HttpServletRequest httpServletRequest;
    
    public void revoke() {
        String authHeader = Optional.ofNullable(httpServletRequest.getHeader("Authorization"))
                .orElseThrow(IllegalStateException::new);

        String jti = jwtProvider.extractJti(authHeader);
        Duration ttl = jwtProvider.extractTimeUntilExpiration(authHeader);
        cacheManager.save(jti, ttl);
    }

    public boolean isRevoked(String authHeader) {
        String jti = jwtProvider.extractJti(authHeader);
        return cacheManager.isPresent(jti);
    }
}
