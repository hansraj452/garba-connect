package com.garbaconnect.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "connections")
public class Connection {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @Column(name = "connected_at", nullable = false, updatable = false)
    private Instant connectedAt;

    @OneToOne(mappedBy = "connection", fetch = FetchType.LAZY)
    private Conversation conversation;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (connectedAt == null) {
            connectedAt = Instant.now();
        }
    }

    /** Canonical ordering required by DB check: user_a_id &lt; user_b_id. */
    public static Connection of(User first, User second) {
        UUID idA = first.getId();
        UUID idB = second.getId();
        if (idA == null || idB == null) {
            throw new IllegalArgumentException("Users must be persisted before creating a connection");
        }
        Connection connection = new Connection();
        if (idA.compareTo(idB) < 0) {
            connection.userA = first;
            connection.userB = second;
        } else {
            connection.userA = second;
            connection.userB = first;
        }
        return connection;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUserA() {
        return userA;
    }

    public User getUserB() {
        return userB;
    }

    public Instant getConnectedAt() {
        return connectedAt;
    }

    public Conversation getConversation() {
        return conversation;
    }
}
