package com.garbaconnect.repository;

import com.garbaconnect.domain.entity.UserProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByPhone(String phone);

    boolean existsByPhone(String phone);
    Optional<UserProfile> findByUserId(UUID userId);
}
