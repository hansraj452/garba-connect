package com.garbaconnect.repository;

import com.garbaconnect.domain.entity.ConnectionRequest;
import com.garbaconnect.domain.enums.ConnectionRequestStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, UUID> {

    List<ConnectionRequest> findByReceiver_IdAndStatus(UUID receiverId, ConnectionRequestStatus status);

    List<ConnectionRequest> findBySender_IdAndStatus(UUID senderId, ConnectionRequestStatus status);

    Optional<ConnectionRequest> findBySender_IdAndReceiver_IdAndStatus(
            UUID senderId, UUID receiverId, ConnectionRequestStatus status);

    boolean existsBySender_IdAndReceiver_IdAndStatus(
            UUID senderId, UUID receiverId, ConnectionRequestStatus status);
}
