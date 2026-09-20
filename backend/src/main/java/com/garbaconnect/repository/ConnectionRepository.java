package com.garbaconnect.repository;

import com.garbaconnect.domain.entity.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConnectionRepository extends JpaRepository<Connection, UUID> {

    Optional<Connection> findByUserAIdAndUserBId(UUID userAId, UUID userBId);

    @Query(
            """
            select c from Connection c
            where c.userA.id = :userId or c.userB.id = :userId
            """)
    List<Connection> findAllForUser(@Param("userId") UUID userId);

    @Query(
            """
            select c from Connection c
            where (c.userA.id = :firstId and c.userB.id = :secondId)
               or (c.userA.id = :secondId and c.userB.id = :firstId)
            """)
    Optional<Connection> findBetweenUsers(@Param("firstId") UUID firstId, @Param("secondId") UUID secondId);
}
