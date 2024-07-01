package com.linked.classbridge.dto.chat;

import com.linked.classbridge.domain.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class CreateChatRoom {

    public record Request(
            @Schema(description = "채팅 상대 유저 ID", example = "1")
            @NotNull(message = "유저 ID는 필수 입력 값입니다.")
            Long userId
    ) {

    }

    public record Response(
            @Schema(description = "채팅방 ID", example = "1")
            Long chatRoomId,
            @Schema(description = "채팅방 URL", example = "/chatRooms/{chatRoomId}")
            String chatRoomUrl
    ) {

        public static Response fromEntity(ChatRoom chatRoom) {
            return new Response(
                    chatRoom.getChatRoomId(),
                    "/chatRooms/" + chatRoom.getChatRoomId());
        }
    }

}
