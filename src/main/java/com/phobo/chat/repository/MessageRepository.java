package com.phobo.chat.repository;

import com.phobo.chat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    // Lấy tin nhắn theo cuộc hội thoại, phân trang để load-more
    @Query("SELECT m FROM Message m " +
            "JOIN FETCH m.sender " +
            "WHERE m.conversation.conversationId = :convId " +
            "ORDER BY m.createdAt DESC")
    Slice<Message> findMessagesByConversation(@Param("convId") UUID convId, Pageable pageable);
}
