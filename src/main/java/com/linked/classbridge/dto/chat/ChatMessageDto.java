package com.linked.classbridge.dto.chat;

import com.linked.classbridge.domain.ChatMessage;

public record ChatMessageDto(
        String messageId,
        Long senderId,
        String senderNickName,
        String message,
        boolean isRead,
        String sendTime
) {
    public static ChatMessageDto fromEntity(ChatMessage chatMessage) {
        return new ChatMessageDto(
                chatMessage.getId(),
                chatMessage.getSender().getUserId(),
                chatMessage.getSender().getNickname(),
                chatMessage.getMessage(),
                chatMessage.isRead(),
                chatMessage.getSendTime().toString()
        );
    }
}
