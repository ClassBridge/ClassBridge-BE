package com.linked.classbridge.websocket;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.service.JWTService;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.service.chat.UserChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final JWTService jwtService;

    private final UserService userService;

    private final UserChatRoomService userChatRoomService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() == StompCommand.DISCONNECT) {
            String token = accessor.getFirstNativeHeader("access");
            log.info("DISCONNECT command received for token: {}", token);

            if (token != null && !jwtService.isExpired(token)) {
                String email = jwtService.getEmail(token);
                User user = userService.getUserByEmail(email);

                log.info("User {} is leaving all chat rooms", user.getUserId());
                userChatRoomService.leaveAllChatRoomsByEmail(user);
                return message;
            }
        }

        return message;
    }
}
