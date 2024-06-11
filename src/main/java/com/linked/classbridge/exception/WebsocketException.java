package com.linked.classbridge.exception;

import com.linked.classbridge.type.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class WebsocketException extends RuntimeException {
    
    private final ErrorCode errorCode;
}
