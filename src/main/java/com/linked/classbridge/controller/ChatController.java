package com.linked.classbridge.controller;

import com.linked.classbridge.dto.ChatMessageDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessageDto sendMessage(ChatMessageDto message) {
        return message;
    }
}
