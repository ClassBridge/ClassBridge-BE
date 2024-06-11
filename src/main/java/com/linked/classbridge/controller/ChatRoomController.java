package com.linked.classbridge.controller;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.chat.CreateChatRoom;
import com.linked.classbridge.dto.chat.JoinChatRoom;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.ChatRoomService;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.ErrorCode;
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

    private final ChatRoomService chatRoomService;

    private final UserService userService;

    @Operation(summary = "채팅방 생성", description = "채팅방을 생성합니다.")
    @PostMapping
    public ResponseEntity<SuccessResponse<CreateChatRoom.Response>> createChatRoom(
            @Valid @RequestBody CreateChatRoom.Request request
    ) {

        User user = userService.findByEmail(userService.getCurrentUserEmail())
                .orElseThrow(() -> new RestApiException(ErrorCode.USER_NOT_FOUND));
        // 채팅방 생성
        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.CHAT_ROOM_CREATE_SUCCESS,
                        chatRoomService.createOrGetChatRoom(user, request.classId())
                )
        );
    }

    @Operation(summary = "채팅방 참여", description = "채팅방에 참여합니다.")
    @GetMapping("/{chatRoomId}/join")
    public ResponseEntity<SuccessResponse<JoinChatRoom.Response>> joinChatRoom(
            @PathVariable Long chatRoomId
    ) {
        User user = userService.findByEmail(userService.getCurrentUserEmail())
                .orElseThrow(() -> new RestApiException(ErrorCode.USER_NOT_FOUND));
        // 채팅방 참여
        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.CHAT_ROOM_JOIN_SUCCESS,
                        chatRoomService.joinChatRoomAndGetMessages(user, chatRoomId)
                )
        );
    }

    @Operation(summary = "채팅방 목록 조회", description = "채팅방 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<?> getChatRooms() {
        User user = userService.findByEmail(userService.getCurrentUserEmail())
                .orElseThrow(() -> new RestApiException(ErrorCode.USER_NOT_FOUND));

        return ResponseEntity.ok().body(SuccessResponse.of(chatRoomService.getChatRooms(user)));
    }
}
