package com.garbaconnect.service;

import com.garbaconnect.domain.dto.*;
import com.garbaconnect.domain.entity.*;
import com.garbaconnect.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.garbaconnect.service.FileStorageService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;

@Service
@Transactional
public class ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final FileStorageService fileStorageService;

    public ProfileService(
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            FileStorageService fileStorageService){

        this.userRepository=userRepository;
        this.profileRepository=profileRepository;
        this.fileStorageService=fileStorageService;
    }

    private User currentUser() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email).orElseThrow();
    }

    public UserProfileResponse getMyProfile() {

        User user = currentUser();

        UserProfile profile = profileRepository
                .findByUserId(user.getId())
                .orElseGet(() -> {

                    UserProfile p = new UserProfile();
                    p.setUser(user); // userId is set automatically

                    return profileRepository.save(p);
                });

        UserProfileResponse response = new UserProfileResponse();

        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setProfileImageUrl(profile.getProfileImageUrl());
        response.setPhone(profile.getPhone());
        response.setLocation(profile.getLocation());
        response.setHeightCm(profile.getHeightCm());
        response.setComplexion(profile.getComplexion());
        response.setBio(profile.getBio());
        response.setFavoriteGarbaStyle(profile.getFavoriteGarbaStyle());

        return response;
    }

    public UserProfileResponse updateProfile(UpdateProfileRequest request) {

        User user = currentUser();

        UserProfile profile = profileRepository
                .findByUserId(user.getId())
                .orElseGet(() -> {

                    UserProfile p = new UserProfile();
                    p.setUser(user); // userId is set automatically

                    return profileRepository.save(p);
                });

        profile.setPhone(request.getPhone());
        profile.setLocation(request.getLocation());
        profile.setHeightCm(request.getHeightCm());
        profile.setComplexion(request.getComplexion());
        profile.setBio(request.getBio());
        profile.setFavoriteGarbaStyle(request.getFavoriteGarbaStyle());

        profileRepository.save(profile);

        return getMyProfile();
    }

    public ImageUploadResponse uploadImage(MultipartFile file) throws IOException {

        validateImage(file);

        User user = currentUser();

        UserProfile profile = profileRepository
                .findByUserId(user.getId())
                .orElseThrow();

        if(profile.getProfileImageUrl()!=null){

            fileStorageService.deleteImage(profile.getProfileImageUrl());
        }

        String url=fileStorageService.uploadProfileImage(file);

        profile.setProfileImageUrl(url);

        profileRepository.save(profile);

        return new ImageUploadResponse(url);
    }

    private void validateImage(MultipartFile file) {

        if (file.isEmpty())
            throw new RuntimeException("File is empty");

        if (file.getSize() > 5 * 1024 * 1024)
            throw new RuntimeException("Maximum size is 5MB");

        String type = file.getContentType();

        if (type == null || !type.startsWith("image/"))
            throw new RuntimeException("Only image files allowed");
    }
}