package com.garbaconnect.domain.dto;

import java.math.BigDecimal;

public class UpdateProfileRequest {

    private String phone;
    private String location;
    private BigDecimal heightCm;
    private String complexion;
    private String bio;
    private String favoriteGarbaStyle;

    public UpdateProfileRequest() {}

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }

    public String getComplexion() { return complexion; }
    public void setComplexion(String complexion) { this.complexion = complexion; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getFavoriteGarbaStyle() { return favoriteGarbaStyle; }
    public void setFavoriteGarbaStyle(String favoriteGarbaStyle) { this.favoriteGarbaStyle = favoriteGarbaStyle; }
}