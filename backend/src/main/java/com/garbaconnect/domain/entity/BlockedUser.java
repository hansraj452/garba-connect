package com.garbaconnect.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "blocked_users")
public class BlockedUser {

    @EmbeddedId
    private BlockedUserId id = new BlockedUserId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("blockerId")
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("blockedId")
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public static BlockedUser of(User blocker, User blocked) {
        BlockedUser edge = new BlockedUser();
        edge.blocker = blocker;
        edge.blocked = blocked;
        edge.id = new BlockedUserId(blocker.getId(), blocked.getId());
        return edge;
    }

    public BlockedUserId getId() {
        return id;
    }

    public User getBlocker() {
        return blocker;
    }

    public User getBlocked() {
        return blocked;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Embeddable
    public static class BlockedUserId implements Serializable {

        @Column(name = "blocker_id", nullable = false)
        private UUID blockerId;

        @Column(name = "blocked_id", nullable = false)
        private UUID blockedId;

        protected BlockedUserId() {}

        public BlockedUserId(UUID blockerId, UUID blockedId) {
            this.blockerId = blockerId;
            this.blockedId = blockedId;
        }

        public UUID getBlockerId() {
            return blockerId;
        }

        public UUID getBlockedId() {
            return blockedId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof BlockedUserId that)) {
                return false;
            }
            return Objects.equals(blockerId, that.blockerId) && Objects.equals(blockedId, that.blockedId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockerId, blockedId);
        }
    }
}
