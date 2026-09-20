package com.garbaconnect.repository;

import com.garbaconnect.domain.entity.Conversation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByConnectionId(UUID connectionId);
}
