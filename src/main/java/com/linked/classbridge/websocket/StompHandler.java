package com.linked.classbridge.websocket;

import com.linked.classbridge.service.JWTService;
import com.linked.classbridge.service.UserChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final JWTService jwtService;

    private final UserChatRoomService userChatRoomService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() == StompCommand.DISCONNECT) {
            String token = accessor.getFirstNativeHeader("access");

            if (token != null && !jwtService.isExpired(token)) {
                String email = jwtService.getEmail(token);
                userChatRoomService.leaveAllChatRoomsByEmail(email);
                return message;
            }
        }

        return message;
    }
}
