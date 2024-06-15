package com.linked.classbridge.config;

import com.linked.classbridge.websocket.CustomHandshakeHandler;
import com.linked.classbridge.websocket.HttpHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    private final HttpHandshakeInterceptor httpHandshakeInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/chatRooms", "/chatRoom", "/read");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/CB-websocket")
                .setAllowedOrigins("http://localhost:3000")
                .addInterceptors(httpHandshakeInterceptor)
                .setHandshakeHandler(new CustomHandshakeHandler())
                .withSockJS();
    }
}
