package ru.halcyon.meetingease.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.halcyon.meetingease.exception.TokenVerificationException;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.security.JwtProvider;
import ru.halcyon.meetingease.service.auth.TokenRevocationService;
import ru.halcyon.meetingease.util.JwtUtil;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final TokenRevocationService tokenRevocationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwtToken = getTokenFromRequest(request);

        if (jwtToken != null && jwtProvider.isValidAccessToken(jwtToken)) {

            if (tokenRevocationService.isRevoked(jwtToken)) {
                throw new TokenVerificationException();
            }

            Claims claims = jwtProvider.extractAllClaims(jwtToken);

            JwtAuthentication jwtAuth = JwtUtil.getAuthentication(claims);
            jwtAuth.setAuthenticated(true);

            SecurityContextHolder.getContext().setAuthentication(jwtAuth);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
