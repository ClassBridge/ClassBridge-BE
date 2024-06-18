package com.linked.classbridge.service.chat;

import com.linked.classbridge.domain.ChatMessage;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.ChatMessageRepository;
import com.linked.classbridge.type.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage saveMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> findLatestChatMessagesByChatRoom(Long chatRoomId) {
        return chatMessageRepository.findByChatRoomIdOrderBySendTimeAsc(chatRoomId);
    }

    public List<ChatMessage> findMessagesUserNotRead(Long chatRoomId, Long userId) {
        return chatMessageRepository.findByChatRoomIdAndSenderIdNotAndIsReadFalse(chatRoomId, userId);
    }

    public ChatMessage findChatMessageById(String messageId) {
        return chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RestApiException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));
    }

    public ChatMessage findAllMessageByChatRoomIdOrderBySendTimeDesc(Long chatRoomId) {
        return chatMessageRepository.findByChatRoomIdOrderBySendTimeDesc(chatRoomId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public void markAsReadAndSave(ChatMessage chatMessage) {
        chatMessage.readMessage();
        saveMessage(chatMessage);
    }

    public List<ChatMessage> markSentByOtherChatMessagesAsRead(List<ChatMessage> chatMessages, Long userId) {
        return chatMessages.stream()
                .filter(chatMessage -> !chatMessage.isRead() && !chatMessage.getSenderId().equals(userId))
                .peek(this::markAsReadAndSave)
                .toList();
    }

}
