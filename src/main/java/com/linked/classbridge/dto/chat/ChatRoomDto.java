package com.linked.classbridge.dto.chat;

import com.linked.classbridge.domain.ChatRoom;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDto {

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

    }

    public void addInquiredChatRoom(ChatRoom chatRoom) {
        inquiredChatRoomsChatRooms.add(inquiredChatRooms.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .inquiredUserId(chatRoom.getInitiatedBy().getUserId())
                .tutorUserId(chatRoom.getInitiatedTo().getUserId())
                .tutorNickname(chatRoom.getInitiatedTo().getNickname())
                .tutorProfileImageUrl(chatRoom.getInitiatedTo().getProfileImageUrl())
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
    }

    public void addReceivedInquiryChatRoom(ChatRoom chatRoom) {
        receivedInquiryChatRoomsChatRooms.add(receivedInquiryChatRooms.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .inquiredUserId(chatRoom.getInitiatedBy().getUserId())
                .inquiredUserNickname(chatRoom.getInitiatedBy().getNickname())
                .inquiredUserProfileImageUrl(chatRoom.getInitiatedBy().getProfileImageUrl())
                .tutorUserId(chatRoom.getInitiatedTo().getUserId())
                .build());
    }
}
