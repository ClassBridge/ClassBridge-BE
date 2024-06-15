package com.linked.classbridge.dto.chat;

import com.linked.classbridge.domain.ChatRoom;
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
public class ChatRoomDto {

    @Setter
    private Long userId;

    @Builder.Default
    private List<inquiredChatRooms> inquiredChatRoomsChatRooms = new ArrayList<>();

    @Builder.Default
    private List<receivedInquiryChatRooms> receivedInquiryChatRoomsChatRooms = new ArrayList<>();


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class inquiredChatRooms {

        private Long chatRoomId;

        private Long inquiredUserId;

        private Long tutorUserId;

        private String tutorNickname;

        private String tutorProfileImageUrl;

        private ChatRoomUnreadCountInfoDto unreadCountInfo;

    }

    public void addInquiredChatRoom(ChatRoom chatRoom, ChatRoomUnreadCountInfoDto unreadCountInfo) {
        inquiredChatRoomsChatRooms.add(inquiredChatRooms.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .inquiredUserId(chatRoom.getInitiatedBy().getUserId())
                .tutorUserId(chatRoom.getInitiatedTo().getUserId())
                .tutorNickname(chatRoom.getInitiatedTo().getNickname())
                .tutorProfileImageUrl(chatRoom.getInitiatedTo().getProfileImageUrl())
                .unreadCountInfo(unreadCountInfo)
                .build());
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class receivedInquiryChatRooms {
        private Long chatRoomId;

        private Long inquiredUserId;

        private String inquiredUserNickname;

        private String inquiredUserProfileImageUrl;

        private Long tutorUserId;

        private ChatRoomUnreadCountInfoDto unreadCountInfo;
    }

    public void addReceivedInquiryChatRoom(ChatRoom chatRoom, ChatRoomUnreadCountInfoDto unreadCountInfo) {
        receivedInquiryChatRoomsChatRooms.add(receivedInquiryChatRooms.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .inquiredUserId(chatRoom.getInitiatedBy().getUserId())
                .inquiredUserNickname(chatRoom.getInitiatedBy().getNickname())
                .inquiredUserProfileImageUrl(chatRoom.getInitiatedBy().getProfileImageUrl())
                .tutorUserId(chatRoom.getInitiatedTo().getUserId())
                .unreadCountInfo(unreadCountInfo)
                .build());
    }
}
