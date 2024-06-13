package com.linked.classbridge.dto.chat;

import java.time.LocalDateTime;
import java.util.List;

public class JoinChatRoom {

    public record Response(

            Long chatRoomId,
            Long senderId,
            Long initiatedBy,
            Long initiatedTo,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<ChatMessageDto> messages
    ) {
        public static Response of(Long chatRoomId, Long senderId, Long initiatedBy, Long initiatedTo,
                                  LocalDateTime createdAt, LocalDateTime updatedAt, List<ChatMessageDto> messages) {
            return new Response(
                    chatRoomId,
                    senderId,
                    initiatedBy,
                    initiatedTo,
                    createdAt,
                    updatedAt,
                    messages
            );
        }

    }

}
