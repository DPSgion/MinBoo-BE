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
    // 1. Dùng cho lần đầu tiên mở khung chat (Lấy tin mới nhất)
    @Query("SELECT m FROM Message m " +
            "JOIN FETCH m.sender " +
            "WHERE m.conversation.conversationId = :convId " +
            "ORDER BY m.createdAt DESC")
    Slice<Message> findMessagesByConversation(@Param("convId") UUID convId, Pageable pageable);

    // 2. Dùng khi vuốt lên để xem tin cũ hơn (Load More)
    @Query("SELECT m FROM Message m " +
            "JOIN FETCH m.sender " +
            "WHERE m.conversation.conversationId = :convId " +
            "AND m.createdAt < :beforeTime " +
            "ORDER BY m.createdAt DESC")
    Slice<Message> findMessagesBeforeTime(
            @Param("convId") UUID convId,
            @Param("beforeTime") java.time.ZonedDateTime beforeTime,
            Pageable pageable);
}
