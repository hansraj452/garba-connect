package com.garbaconnect.repository;

import com.garbaconnect.domain.entity.Message;
import com.garbaconnect.domain.enums.MessageReadStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    long countByReceiverIdAndReadStatusNot(UUID receiverId, MessageReadStatus readStatus);

    List<Message> findByReceiverIdAndReadStatus(UUID receiverId, MessageReadStatus readStatus);
}
