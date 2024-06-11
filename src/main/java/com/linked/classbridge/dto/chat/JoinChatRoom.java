package com.linked.classbridge.dto.chat;

import com.linked.classbridge.domain.ChatMessage;
import com.linked.classbridge.domain.ChatRoom;
import java.time.LocalDateTime;
import java.util.List;

public class JoinChatRoom {

    public record Response(

            Long chatRoomId,
            Long initiatedBy,
            Long initiatedTo,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<ChatMessageDto> chatMessages
    ) {
        public static Response fromEntity(ChatRoom chatRoom, List<ChatMessage> chatMessages) {
            return new Response(
                    chatRoom.getChatRoomId(),
                    chatRoom.getInitiatedBy().getUserId(),
                    chatRoom.getInitiatedTo().getUserId(),
                    chatRoom.getCreatedAt(),
                    chatRoom.getUpdatedAt(),
                    chatMessages.stream()
                            .map(ChatMessageDto::fromEntity)
                            .toList()
            );
        }

    }

}
