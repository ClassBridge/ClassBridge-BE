package com.linked.classbridge.repository;

import com.linked.classbridge.domain.ChatMessage;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByChatRoomIdOrderBySendTimeAsc(Long chatRoomId);
    
    List<ChatMessage> findByChatRoomIdAndSenderIdNotAndIsReadFalse(Long chatRoomId, Long userId);

    List<ChatMessage> findByChatRoomIdOrderBySendTimeDesc(Long chatRoomId);
}
