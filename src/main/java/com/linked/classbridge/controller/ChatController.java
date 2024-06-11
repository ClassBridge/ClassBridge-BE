package com.linked.classbridge.controller;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.ChatMessageDto;
import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.chat.CreateChatRoom;
import com.linked.classbridge.service.ChatRoomService;
import com.linked.classbridge.service.ChatService;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatRooms")
public class ChatController {

    private final ChatService chatService;

    private final ChatRoomService chatRoomService;

    @Operation(summary = "채팅방 생성", description = "채팅방을 생성합니다.")
    @PostMapping
    public ResponseEntity<SuccessResponse<CreateChatRoom.Response>> createChatRoom(
            @Valid @RequestBody CreateChatRoom.Request request
    ) {
        User user = User.builder().userId(1L).build();
        // 채팅방 생성
        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.CHAT_ROOM_CREATE_SUCCESS,
                        chatRoomService.createOrGetChatRoom(user, request.classId())
                )
        );
    }

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessageDto sendMessage(ChatMessageDto message) {
        return message;
    }
}
