package com.phobo.friends.repository;

import com.phobo.friends.entity.FriendRequestEntity;
import com.phobo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequestEntity, Integer> {

    List<FriendRequestEntity> findAllByRequesterAndStatusOrderByCreatedAtDesc(User requester, FriendRequestEntity.RequestStatus status);

    List<FriendRequestEntity> findAllByReceiverAndStatusOrderByCreatedAtDesc(User receiver, FriendRequestEntity.RequestStatus status);

    @Query("""
        SELECT f FROM FriendRequestEntity f
        WHERE (f.requester.id = :userA AND f.receiver.id = :userB)
           OR (f.requester.id = :userB AND f.receiver.id = :userA)
    """)
    Optional<FriendRequestEntity> findRequestBetween(@Param("userA") UUID userA, @Param("userB") UUID userB);
}