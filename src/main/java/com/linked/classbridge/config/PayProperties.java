package com.linked.classbridge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "pay")
public class PayProperties {
    private String devKey;
    private String cid;     // 가맹점 코드
    private String readyUrl;
    private String approveUrl;
}
