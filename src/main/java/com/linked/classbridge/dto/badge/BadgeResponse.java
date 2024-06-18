package com.linked.classbridge.dto.badge;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BadgeResponse {

        private String name;
        private String imageUrl;
}
