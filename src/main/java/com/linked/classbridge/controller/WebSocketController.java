package com.linked.classbridge.controller;

import com.linked.classbridge.dto.chat.SendMessageDto;
import com.linked.classbridge.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatService chatService;

    @MessageMapping("/{chatRoomId}")
    public void sendMessage(
            @DestinationVariable Long chatRoomId, SendMessageDto sendRequest) {
        chatService.sendMessage(sendRequest.senderEmail(), chatRoomId, sendRequest.message());
    }

}
