package com.phobo.chat.repository;

import com.phobo.chat.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    //tim cuoc hoi thoai cua 2 nguoi
    @Query("SELECT c FROM Conversation c " +
            "JOIN FETCH c.userOne " +
            "JOIN FETCH c.userTwo " +
            "WHERE c.userOne.id = :id1 AND c.userTwo.id = :id2")
    Optional<Conversation> findBySortedUsers(@Param("id1") UUID id1, @Param("id2") UUID id2);

    //lay sanh sach cuoc hoi thoai
    @Query("SELECT c FROM Conversation c " +
            "JOIN FETCH c.userOne " +
            "JOIN FETCH c.userTwo " +
            "WHERE c.userOne.id = :userId OR c.userTwo.id = :userId " +
            "ORDER BY c.updatedAt DESC")
    List<Conversation> findAllByUserId(@Param("userId") UUID userId);
}
