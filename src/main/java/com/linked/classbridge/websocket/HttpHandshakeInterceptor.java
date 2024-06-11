package com.linked.classbridge.websocket;

import com.linked.classbridge.dto.user.UserDto;
import com.linked.classbridge.exception.WebsocketException;
import com.linked.classbridge.security.CustomUserDetails;
import com.linked.classbridge.service.JWTService;
import com.linked.classbridge.type.ErrorCode;
import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@RequiredArgsConstructor
@Component
public class HttpHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTService jwtService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        String token = parseTokenFromUri(request.getURI());
        if (token != null && !jwtService.isExpired(token)) {
            if (attributes.get("user") == null) {
                UserDto user = new UserDto();
                user.setEmail(jwtService.getEmail(token));
                user.setRoles(jwtService.getRoles(token));

                CustomUserDetails userDetails = new CustomUserDetails(user);
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                attributes.put("user", authentication);
            }
            return true;
        }
        throw new WebsocketException(ErrorCode.INVALID_TOKEN);
    }


    @Override
    public void afterHandshake(
            ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }

    private String parseTokenFromUri(URI uri) {
        return uri.getQuery().split("=")[1].split("&")[0];
    }
}
