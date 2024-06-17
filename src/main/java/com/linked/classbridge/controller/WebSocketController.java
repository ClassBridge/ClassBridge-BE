package com.linked.classbridge.controller;

import com.linked.classbridge.dto.chat.SendMessageDto;
import com.linked.classbridge.exception.WebsocketException;
import com.linked.classbridge.service.chat.ChatService;
import com.linked.classbridge.type.ErrorCode;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatService chatService;

    @MessageMapping("/send/{chatRoomId}")
    public void sendMessage(
            @DestinationVariable Long chatRoomId,
            @Payload SendMessageDto sendRequest,
            Principal principal) {

        if (principal == null) {
            throw new WebsocketException(ErrorCode.UNAUTHORIZED);
        }

        chatService.sendMessageProcess(principal.getName(), chatRoomId, sendRequest.message());
    }

    @MessageMapping("/read/{messageId}")
    public void markAsRead(
            @DestinationVariable String messageId,
            Principal principal) {

        if (principal == null) {
            throw new WebsocketException(ErrorCode.UNAUTHORIZED);
        }

        chatService.markMessageAsReadAndSendReceipt(principal.getName(), messageId);
    }

}
