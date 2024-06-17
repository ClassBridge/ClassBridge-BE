package com.linked.classbridge.dto.chat;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadReceipt {

    private String chatMessageId;
    private Long userId;

    @Override
    public int hashCode() {
        return Objects.hash(chatMessageId, userId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ReadReceipt that = (ReadReceipt) obj;
        return chatMessageId.equals(that.chatMessageId) && userId.equals(that.userId);
    }
}
