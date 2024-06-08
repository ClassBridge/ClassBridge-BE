package com.linked.classbridge.type;

public enum TokenType {
    ACCESS("access", 600000L), // 10분
    REFRESH("refresh", 86400000L); // 24시간

    private final String value;
    private final long expiryTime;

    TokenType(String value, long expiryTime) {
        this.value = value;
        this.expiryTime = expiryTime;
    }

    public String getValue() {
        return value;
    }

    public long getExpiryTime() {
        return expiryTime;
    }
}
