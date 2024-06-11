package com.linked.classbridge.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/chatRooms", "chatRoom"); //
        config.setApplicationDestinationPrefixes("/send"); //
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/CB-websocket")
                .setAllowedOrigins("http://localhost:3000")
                //.addInterceptors(new JwtHandshakeInterceptor()) // TODO JWT 인증을 위한 인터셉터 추가
                .withSockJS();
    }
}
