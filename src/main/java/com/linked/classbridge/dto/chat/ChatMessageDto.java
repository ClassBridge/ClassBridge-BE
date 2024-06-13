package com.linked.classbridge.dto.chat;

import com.linked.classbridge.domain.ChatMessage;

public record ChatMessageDto(
        String messageId,
        Long senderId,
        String message,
        boolean isRead,
        String sendTime
) {
    public static ChatMessageDto fromEntity(ChatMessage chatMessage) {
        return new ChatMessageDto(
                chatMessage.getId(),
                chatMessage.getSenderId(),
                chatMessage.getMessage(),
                chatMessage.isRead(),
                chatMessage.getSendTime().toString()
        );
    }
}
