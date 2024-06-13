package com.linked.classbridge.websocket;

import java.security.Principal;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class StompPrincipal implements Principal {

    private final String name;

    private final List<String> roles;
}
