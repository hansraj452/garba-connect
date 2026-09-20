package com.garbaconnect.repository;

import com.garbaconnect.domain.entity.Notification;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Notification> findByUser_IdAndReadOrderByCreatedAtDesc(UUID userId, boolean read, Pageable pageable);

    long countByUser_IdAndReadFalse(UUID userId);
}
