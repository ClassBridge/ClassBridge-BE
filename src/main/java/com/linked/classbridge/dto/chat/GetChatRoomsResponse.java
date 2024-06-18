package com.linked.classbridge.dto.chat;

import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetChatRoomsResponse {

    @Setter
    private Long userId;

    @Builder.Default
    private List<ChatRoomInfoDto> chatRooms = new ArrayList<>();

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatRoomInfoDto {
        private Long chatRoomId;
        private Long chatPartnerId;
        private String chatPartnerNickname;
        private String chatPartnerProfileImageUrl;
        private ChatRoomUnreadCountInfoDto unreadCountInfo;
    }

    public void addChatRoomInfo(ChatRoom chatRoom, User chatPartner, ChatRoomUnreadCountInfoDto unreadCountInfo) {
        chatRooms.add(ChatRoomInfoDto.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .chatPartnerId(chatPartner.getUserId())
                .chatPartnerNickname(chatPartner.getNickname())
                .chatPartnerProfileImageUrl(chatPartner.getProfileImageUrl())
                .unreadCountInfo(unreadCountInfo)
                .build());
    }

}
