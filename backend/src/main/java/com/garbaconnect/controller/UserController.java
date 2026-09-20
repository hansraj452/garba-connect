package com.garbaconnect.controller;

import com.garbaconnect.domain.entity.User;
import com.garbaconnect.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.garbaconnect.service.ProfileService;
import org.springframework.http.ResponseEntity;

import com.garbaconnect.domain.dto.UserProfileResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import com.garbaconnect.domain.dto.UpdateProfileRequest;

import com.garbaconnect.domain.dto.ImageUploadResponse;

import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private final ProfileService profileService;

    public UserController(ProfileService profileService) {
        this.profileService = profileService;
    }
    @GetMapping("/discover")
    public String discover() {
        return "Discover Users";
    }

    @PostMapping("/connect")
    public String connect() {
        return "Connection Sent";
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(profileService.getMyProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(profileService.updateProfile(request));
    }

    @PostMapping("/profile/image")
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        return ResponseEntity.ok(profileService.uploadImage(file));
    }
}