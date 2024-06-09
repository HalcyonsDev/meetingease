package ru.halcyon.meetingease.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.halcyon.meetingease.config.TokenConfigProperties;
import ru.halcyon.meetingease.exception.InvalidKeyException;
import ru.halcyon.meetingease.model.Agent;
import ru.halcyon.meetingease.model.Client;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@EnableConfigurationProperties(TokenConfigProperties.class)
public class JwtProvider {
    private final TokenConfigProperties tokenConfigProperties;
    private final String issuer;

    public JwtProvider(
            TokenConfigProperties tokenConfigProperties,
            @Value("${spring.application.name}") String issuer
    ) {
        this.tokenConfigProperties = tokenConfigProperties;
        this.issuer = issuer;
    }

    public String generateAccessTokenForClient(Client client) {
        Map<String, Boolean> extraClaims = new HashMap<>();
        extraClaims.put("isClient", true);

        return generateAccessToken(client.getEmail(), extraClaims);
    }

    public String generateAccessTokenForAgent(Agent agent) {
        Map<String, Boolean> extraClaims = new HashMap<>();
        extraClaims.put("isClient", false);

        return generateAccessToken(agent.getEmail(), extraClaims);
    }

    private String generateAccessToken(String email, Map<String, Boolean> extraClaims) {
        String jti = String.valueOf(UUID.randomUUID());
        Date currentTimestamp = new Date(System.currentTimeMillis());
        Integer accessTokenValidity = tokenConfigProperties.getAccessToken().getValidity();
        long expiration = TimeUnit.MINUTES.toMillis(accessTokenValidity);
        Date expirationTimestamp = new Date(System.currentTimeMillis() + expiration);

        return Jwts.builder()
                .claims(extraClaims)
                .id(jti)
                .subject(email)
                .issuer(issuer)
                .issuedAt(currentTimestamp)
                .expiration(expirationTimestamp)
                .signWith(getPrivateKey(), Jwts.SIG.RS512)
                .compact();
    }

    public boolean isValidAccessToken(String jwtToken) {
        try {
            Jwts.parser()
                    .verifyWith(getPublicKey())
                    .build()
                    .parse(jwtToken);

            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public String extractEmail(String token) {
       return extractClaim(token, Claims::getSubject);
    }
    
    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public Duration extractTimeUntilExpiration(String token) {
        Instant expirationTimestamp = extractClaim(token, Claims::getExpiration).toInstant();
        Instant currentTimestamp = new Date().toInstant();
        return Duration.between(currentTimestamp, expirationTimestamp);
    }

    public Claims extractAllClaims(String token) {
        return (Claims) Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parse(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .requireIssuer(issuer)
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private PrivateKey getPrivateKey() {
        String privateKey = tokenConfigProperties.getAccessToken().getPrivateKey();
        String sanitizedPrivateKey = sanitizeKey(privateKey);

        byte[] decodedPrivateKey = Decoders.BASE64.decode(sanitizedPrivateKey);

        try {
            return KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(decodedPrivateKey));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException ex) {
            throw new InvalidKeyException("Failed to generate private key", ex);
        }
    }

    private PublicKey getPublicKey() {
        String publicKey = tokenConfigProperties.getAccessToken().getPublicKey();
        String sanitizedPublicKey = sanitizeKey(publicKey);

        byte[] decodedPublicKey = Decoders.BASE64.decode(sanitizedPublicKey);

        try {
            return KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(decodedPublicKey));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException ex) {
            throw new InvalidKeyException("Failed to generate public key", ex);
        }
    }

    private String sanitizeKey(String key) {
        return key
                .replace("-----BEGIN PUBLIC KEY-----", StringUtils.EMPTY)
                .replace("-----END PUBLIC KEY-----", StringUtils.EMPTY)
                .replace("-----BEGIN PRIVATE KEY-----", StringUtils.EMPTY)
                .replace("-----END PRIVATE KEY-----", StringUtils.EMPTY)
                .replaceAll("\\n", StringUtils.EMPTY)
                .replaceAll("\\s", StringUtils.EMPTY);
    }
}
