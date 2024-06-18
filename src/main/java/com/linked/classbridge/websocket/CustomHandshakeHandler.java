package com.linked.classbridge.websocket;

import com.linked.classbridge.security.CustomUserDetails;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes
    ) {
        Authentication authentication = (Authentication) attributes.get("user");
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return new StompPrincipal(userDetails.getUserDto().getEmail(), userDetails.getUserDto().getRoles());
        }
        return null;
    }
    
}
