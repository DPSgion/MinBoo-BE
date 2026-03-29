package com.phobo.friends.repository;

import com.phobo.friends.entity.FriendShip;
import com.phobo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FriendRepository extends JpaRepository<FriendShip, Integer> {

    @Query("""
        SELECT f.receiver
        FROM FriendShip f
        WHERE f.requester.id = :userId
    """)
    List<User> findSentRequestsByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END 
        FROM FriendShip f 
        WHERE (f.requester.id = :userA AND f.receiver.id = :userB) 
           OR (f.requester.id = :userB AND f.receiver.id = :userA)
    """)
    boolean existsFriendshipBetween(@Param("userA") UUID userA, @Param("userB") UUID userB);

}
