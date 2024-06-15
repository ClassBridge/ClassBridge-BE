package com.linked.classbridge.dto.chat;

import java.time.LocalDateTime;
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

}
