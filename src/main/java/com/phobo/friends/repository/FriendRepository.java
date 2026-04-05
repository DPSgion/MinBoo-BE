package com.phobo.friends.repository;

import com.phobo.friends.entity.Friend;
import com.phobo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRepository extends JpaRepository<Friend, Integer> {

    @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
        FROM Friend f
        WHERE (f.user1.id = :userA AND f.user2.id = :userB)
           OR (f.user1.id = :userB AND f.user2.id = :userA)
    """)
    boolean existsFriendshipBetween(@Param("userA") UUID userA, @Param("userB") UUID userB);

    @Query("""
        SELECT f FROM Friend f
        WHERE (f.user1.id = :userA AND f.user2.id = :userB)
           OR (f.user1.id = :userB AND f.user2.id = :userA)
    """)
    Optional<Friend> findFriendshipBetween(@Param("userA") UUID userA, @Param("userB") UUID userB);

    @Query("""
        SELECT f
        FROM Friend f
        WHERE f.user1 = :user OR f.user2 = :user
    """)
    List<Friend> getFriendList(@Param("user") User user);
}