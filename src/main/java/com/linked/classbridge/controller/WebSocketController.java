package com.linked.classbridge.controller;

import com.linked.classbridge.dto.chat.SendMessageDto;
import com.linked.classbridge.exception.WebsocketException;
import com.linked.classbridge.service.ChatService;
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

    @MessageMapping("/{chatRoomId}")
    public void sendMessage(
            @DestinationVariable Long chatRoomId,
            @Payload SendMessageDto sendRequest,
            Principal principal) {

        if (principal == null) {
            throw new WebsocketException(ErrorCode.UNAUTHORIZED);
        }

        chatService.sendMessage(principal.getName(), chatRoomId, sendRequest.message());
    }

}
