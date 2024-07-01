package com.linked.classbridge.controller;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.chat.CreateChatRoom;
import com.linked.classbridge.dto.chat.GetChatRoomsResponse;
import com.linked.classbridge.dto.chat.JoinChatRoom;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.service.chat.ChatService;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatRooms")
public class ChatRoomController {

    private final ChatService chatService;

    private final UserService userService;

    @Operation(summary = "채팅방 생성", description = "채팅방을 생성합니다.")
    @PostMapping
    public ResponseEntity<SuccessResponse<CreateChatRoom.Response>> createChatRoom(
            @Valid @RequestBody CreateChatRoom.Request request
    ) {
        User user = userService.getCurrentUser();
        // 채팅방 생성
        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.CHAT_ROOM_CREATE_SUCCESS,
                        chatService.createChatRoomProcess(user, request.userId())
                )
        );
    }

    @Operation(summary = "채팅방 입장", description = "채팅방에 입장합니다.")
    @GetMapping("/{chatRoomId}/join")
    public ResponseEntity<SuccessResponse<JoinChatRoom.Response>> joinChatRoom(
            @PathVariable Long chatRoomId
    ) {
        User user = userService.getCurrentUser();
        // 채팅방 참여
        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.CHAT_ROOM_JOIN_SUCCESS,
                        chatService.enterChatRoomProcess(user, chatRoomId)
                )
        );
    }

    @Operation(summary = "채팅방 닫기", description = "채팅방을 닫습니다")
    @PostMapping("/{chatRoomId}/close")
    public ResponseEntity<SuccessResponse<String>> leaveChatRoom(
            @PathVariable Long chatRoomId
    ) {
        User user = userService.getCurrentUser();
        // 채팅방 나가기
        chatService.closeChatRoomProcess(user, chatRoomId);
        return ResponseEntity.ok().body(SuccessResponse.of(ResponseMessage.CHAT_ROOM_CLOSE_SUCCESS));
    }

    @Operation(summary = "채팅방 퇴장", description = "채팅방에서 퇴장합니다.")
    @PostMapping("/{chatRoomId}/leave")
    public ResponseEntity<SuccessResponse<String>> deleteChatRoom(
            @PathVariable Long chatRoomId
    ) {
        User user = userService.getCurrentUser();
        // 채팅방 삭제
        chatService.leaveChatRoomProcess(user, chatRoomId);
        return ResponseEntity.ok().body(SuccessResponse.of(ResponseMessage.CHAT_ROOM_LEAVE_SUCCESS));
    }

    @Operation(summary = "채팅방 목록 조회", description = "채팅방 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<SuccessResponse<GetChatRoomsResponse>> getChatRooms() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok().body(SuccessResponse.of(chatService.getChatRoomListProcess(user)));
    }
}
