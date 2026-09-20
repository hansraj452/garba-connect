package com.garbaconnect.repository;

import com.garbaconnect.domain.entity.BlockedUser;
import com.garbaconnect.domain.entity.BlockedUser.BlockedUserId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlockedUserRepository extends JpaRepository<BlockedUser, BlockedUserId> {

    boolean existsByBlocker_IdAndBlocked_Id(UUID blockerId, UUID blockedId);

    List<BlockedUser> findByBlocker_Id(UUID blockerId);

    List<BlockedUser> findByBlocked_Id(UUID blockedId);

    @Query(
            """
            select case when count(b) > 0 then true else false end
            from BlockedUser b
            where (b.blocker.id = :userA and b.blocked.id = :userB)
               or (b.blocker.id = :userB and b.blocked.id = :userA)
            """)
    boolean isBlockedEitherDirection(@Param("userA") UUID userA, @Param("userB") UUID userB);
}
