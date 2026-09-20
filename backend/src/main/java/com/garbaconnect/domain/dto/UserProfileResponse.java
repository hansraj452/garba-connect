package com.garbaconnect.domain.dto;

import java.math.BigDecimal;

public class UserProfileResponse {

    private String name;
    private String email;
    private String profileImageUrl;
    private String phone;
    private String location;
    private BigDecimal heightCm;
    private String complexion;
    private String bio;
    private String favoriteGarbaStyle;

    public UserProfileResponse() {}

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getPhone() {
        return phone;
    }

    public String getLocation() {
        return location;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public String getComplexion() {
        return complexion;
    }

    public String getBio() {
        return bio;
    }

    public String getFavoriteGarbaStyle() {
        return favoriteGarbaStyle;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public void setComplexion(String complexion) {
        this.complexion = complexion;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setFavoriteGarbaStyle(String favoriteGarbaStyle) {
        this.favoriteGarbaStyle = favoriteGarbaStyle;
    }
}