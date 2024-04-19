package ru.halcyon.meetingease.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.halcyon.meetingease.model.Agent;
import ru.halcyon.meetingease.model.Client;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProvider {
    private final SecretKey accessToken;
    private final SecretKey refreshToken;

    public JwtProvider(
            @Value("${jwt.secret.access}") String accessToken,
            @Value("${jwt.secret.refresh}") String refreshToken
    ) {
        this.accessToken = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessToken));
        this.refreshToken = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshToken));
    }

    public String generateTokenForClient(Client client, boolean isRefresh) {
        Map<String, Boolean> extraClaims = new HashMap<>();
        extraClaims.put("isClient", true);

        return generateToken(client.getEmail(), isRefresh, extraClaims);
    }

    public String generateTokenForAgent(Agent agent, boolean isRefresh) {
        Map<String, Boolean> extraClaims = new HashMap<>();
        extraClaims.put("isClient", false);

        return generateToken(agent.getEmail(), isRefresh, extraClaims);
    }

    private String generateToken(String email, boolean isRefresh, Map<String, Boolean> extraClaims) {
        LocalDateTime now = LocalDateTime.now();

        Instant expirationInstant = isRefresh ?
                now.plusDays(31).atZone(ZoneId.systemDefault()).toInstant() :
                now.plusDays(7).atZone(ZoneId.systemDefault()).toInstant();

        return Jwts.builder()
                .claims().add(extraClaims).and()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(Date.from(expirationInstant))
                .signWith(isRefresh ? refreshToken : accessToken)
                .compact();
    }

    public Claims extractAccessClaims(String jwtToken) {
        return extractAllClaims(jwtToken, accessToken);
    }

    public Claims extractRefreshClaims(String jwtToken) {
        return extractAllClaims(jwtToken, refreshToken);
    }

    private Claims extractAllClaims(String jwtToken, SecretKey secret) {
        return (Claims) Jwts.parser()
                .verifyWith(secret)
                .build()
                .parse(jwtToken)
                .getPayload();
    }

    public boolean isValidAccessToken(String jwtToken) {
        return isValidToken(jwtToken, accessToken);
    }

    public boolean isValidRefreshToken(String jwtToken) {
        return isValidToken(jwtToken, refreshToken);
    }

    private boolean isValidToken(String jwtToken, SecretKey secret) {
        try {
            Jwts.parser()
                    .verifyWith(secret)
                    .build()
                    .parse(jwtToken);

            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
