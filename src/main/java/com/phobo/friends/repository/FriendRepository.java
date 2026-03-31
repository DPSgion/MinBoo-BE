package com.phobo.friends.repository;

import com.phobo.friends.entity.FriendShip;
import com.phobo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRepository extends JpaRepository<FriendShip, Integer> {

    List<FriendShip> findAllByRequesterAndStatusOrderByCreatedAtDesc(User requester, FriendShip.FriendStatus status);

    @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
        FROM FriendShip f
        WHERE (f.requester.id = :userA AND f.receiver.id = :userB)
           OR (f.requester.id = :userB AND f.receiver.id = :userA)
    """)
    boolean existsFriendshipBetween(@Param("userA") UUID userA, @Param("userB") UUID userB);

    @Query("""
    SELECT f FROM FriendShip f
    WHERE (f.requester.id = :userA AND f.receiver.id = :userB)
       OR (f.requester.id = :userB AND f.receiver.id = :userA)
    """)
    Optional<FriendShip> findFriendshipBetween(@Param("userA") UUID userA, @Param("userB") UUID userB);

    @Query("""
        SELECT f
        FROM FriendShip f
        WHERE f.status = 'ACCEPTED'
          AND (f.requester = :user OR f.receiver = :user)
    """)
    List<FriendShip> getFriendList(@Param("user") User user);

    List<FriendShip> findAllByReceiverAndStatusOrderByCreatedAtDesc(User receiver, FriendShip.FriendStatus status);
}
