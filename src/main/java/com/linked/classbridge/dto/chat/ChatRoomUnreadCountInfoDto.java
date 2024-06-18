package com.linked.classbridge.dto.chat;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomUnreadCountInfoDto {
    private Long chatRoomId;
    @Setter
    private int unreadMessageCount;
    private String latestMessage;
    private LocalDateTime latestMessageTime;

    @Override
    public int hashCode() {
        return Objects.hash(chatRoomId, unreadMessageCount, latestMessage, latestMessageTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChatRoomUnreadCountInfoDto that = (ChatRoomUnreadCountInfoDto) obj;
        if (!Objects.equals(chatRoomId, that.chatRoomId)) {
            return false;
        }
        if (unreadMessageCount != that.unreadMessageCount) {
            return false;
        }
        if (!Objects.equals(latestMessage, that.latestMessage)) {
            return false;
        }
        return Objects.equals(latestMessageTime, that.latestMessageTime);
    }
}
