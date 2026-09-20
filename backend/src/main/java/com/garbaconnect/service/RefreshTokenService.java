package com.garbaconnect.service;

import com.garbaconnect.domain.entity.RefreshToken;
import com.garbaconnect.domain.entity.User;
import com.garbaconnect.repository.RefreshTokenRepository;
import com.garbaconnect.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtService jwtService;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    public RefreshTokenService(
            RefreshTokenRepository repository,
            JwtService jwtService) {

        this.repository = repository;
        this.jwtService = jwtService;
    }

    public RefreshToken createRefreshToken(User user) {

        RefreshToken token = new RefreshToken();

        token.setUser(user);
        token.setToken(jwtService.generateRefreshToken(user));
        token.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
        token.setRevoked(false);

        return repository.save(token);
    }

    public RefreshToken verify(String token) {

        RefreshToken refreshToken =
                repository.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }

    public void revoke(String token) {

        repository.findByToken(token).ifPresent(rt -> {

            rt.setRevoked(true);

            repository.save(rt);
        });
    }
}