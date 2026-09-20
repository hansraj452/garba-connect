package com.garbaconnect.service;

import com.garbaconnect.domain.dto.*;
import com.garbaconnect.domain.entity.RefreshToken;
import com.garbaconnect.domain.entity.User;
import com.garbaconnect.domain.enums.UserRole;
import com.garbaconnect.domain.enums.UserStatus;
import com.garbaconnect.repository.UserRepository;
import com.garbaconnect.security.JwtService;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder encoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {

        this.userRepository = userRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthResponse signup(SignupRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {

            throw new RuntimeException("Email already exists");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setAge(request.getAge().shortValue());
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash(encoder.encode(request.getPassword()));

        userRepository.save(user);

        String access = jwtService.generateAccessToken(user);
        RefreshToken refresh =
                refreshTokenService.createRefreshToken(user);

        return new AuthResponse(access, refresh.getToken());
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        User user =
                userRepository.findByEmail(request.getEmail())
                        .orElseThrow();

        String access = jwtService.generateAccessToken(user);

        RefreshToken refresh =
                refreshTokenService.createRefreshToken(user);

        return new AuthResponse(access, refresh.getToken());
    }

    public AuthResponse refresh(RefreshTokenRequest request) {

        RefreshToken refresh =
                refreshTokenService.verify(request.getRefreshToken());

        String access =
                jwtService.generateAccessToken(refresh.getUser());

        return new AuthResponse(access, refresh.getToken());
    }

    public void logout(RefreshTokenRequest request) {

        refreshTokenService.revoke(request.getRefreshToken());
    }
}