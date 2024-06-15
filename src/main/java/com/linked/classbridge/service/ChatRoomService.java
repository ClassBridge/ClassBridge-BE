package com.linked.classbridge.service;

import com.linked.classbridge.domain.ChatMessage;
import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.UserChatRoom;
import com.linked.classbridge.dto.chat.ChatMessageDto;
import com.linked.classbridge.dto.chat.ChatRoomDto;
import com.linked.classbridge.dto.chat.ChatRoomUnreadCountInfoDto;
import com.linked.classbridge.dto.chat.CreateChatRoom;
import com.linked.classbridge.dto.chat.JoinChatRoom;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.ChatRoomRepository;
import com.linked.classbridge.repository.UserChatRoomRepository;
import com.linked.classbridge.type.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    private final UserChatRoomRepository userChatRoomRepository;

    private final OneDayClassService oneDayClassService;

    private final ChatService chatService;

    /**
     * 채팅방을 생성하거나 이미 존재하는 채팅방을 가져온다.
     *
     * @param initiatedBy 채팅방을 생성하는 유저
     * @param classId     채팅방을 생성하는 클래스
     * @return 생성된 채팅방 정보
     */
    public CreateChatRoom.Response createOrGetChatRoom(User initiatedBy, Long classId) {
        User initiatedTo = oneDayClassService.findClassById(classId).getTutor();

        if (initiatedBy.getUserId().equals(initiatedTo.getUserId())) {
            throw new RestApiException(ErrorCode.BAD_REQUEST);
        }

        ChatRoom chatRoom
                = chatRoomRepository.findByInitiatedByAndInitiatedTo(initiatedBy, initiatedTo)
                .orElseGet(() -> createNewChatRoom(initiatedBy, initiatedTo));

        return CreateChatRoom.Response.fromEntity(chatRoom);
    }

    /**
     * 채팅방에 입장하고 메시지를 가져온다.
     *
     * @param user       입장하는 유저
     * @param chatRoomId 입장할 채팅방
     * @return 입장한 채팅방 정보
     */
    public JoinChatRoom.Response joinChatRoomAndGetMessages(User user, Long chatRoomId) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);

        validateUserInChatRoom(user, chatRoom);

        List<ChatMessage> chatMessages = chatService.getChatMessagesAndMarkAsRead(chatRoomId, user.getUserId());

        // TODO : 추후 redis로 관리
        setUserToOnline(user, chatRoom);

        return createJoinChatRoomResponse(chatRoom, user, chatMessages);
    }

    /**
     * 채팅방 목록을 가져온다.
     *
     * @param user 채팅방 목록을 가져올 유저
     * @return 채팅방 목록
     */
    public ChatRoomDto getChatRooms(User user) {
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserOrderByUpdatedAtDesc(user);
        ChatRoomDto chatRoomDto = new ChatRoomDto();
        chatRoomDto.setUserId(user.getUserId());
        for (ChatRoom chatRoom : chatRooms) {
            Long chatRoomId = chatRoom.getChatRoomId();

            ChatMessage latestMessage = chatService.getLatestMessage(chatRoomId);
            int unreadMessageCount = chatService.getUnreadMessages(chatRoomId, user.getUserId()).size();
            ChatRoomUnreadCountInfoDto chatRoomUnreadCountInfoDto = ChatRoomUnreadCountInfoDto.builder()
                    .chatRoomId(chatRoomId)
                    .unreadMessageCount(unreadMessageCount)
                    .latestMessage(latestMessage == null ? "" : latestMessage.getMessage())
                    .latestMessageTime(latestMessage == null ? null : latestMessage.getSendTime())
                    .build();

            if (chatRoom.getInitiatedBy().getUserId().equals(user.getUserId())) {
                chatRoomDto.addInquiredChatRoom(chatRoom, chatRoomUnreadCountInfoDto);
            } else {
                chatRoomDto.addReceivedInquiryChatRoom(chatRoom, chatRoomUnreadCountInfoDto);
            }
        }
        return chatRoomDto;
    }

    private ChatRoom findChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new RestApiException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private void setUserToOnline(User user, ChatRoom chatRoom) {
        chatRoom.getUserChatRooms().stream()
                .filter(userChatRoom -> userChatRoom.getUser().getUserId().equals(user.getUserId()))
                .findFirst()
                .ifPresent(userChatRoom -> {
                    userChatRoom.setOnline();
                    userChatRoomRepository.save(userChatRoom);
                });
    }


    private void validateUserInChatRoom(User user, ChatRoom chatRoom) {
        if (!chatRoom.getInitiatedBy().getUserId().equals(user.getUserId())
                && !chatRoom.getInitiatedTo().getUserId().equals(user.getUserId())) {
            throw new RestApiException(ErrorCode.USER_NOT_IN_CHAT_ROOM);
        }
    }

    private JoinChatRoom.Response createJoinChatRoomResponse(ChatRoom chatRoom, User user,
                                                             List<ChatMessage> chatMessages) {
        return JoinChatRoom.Response.of(
                chatRoom.getChatRoomId(),
                user.getUserId(),
                chatRoom.getInitiatedBy().getUserId(),
                chatRoom.getInitiatedTo().getUserId(),
                chatRoom.getCreatedAt(),
                chatRoom.getUpdatedAt(),
                chatMessages.stream()
                        .map(ChatMessageDto::fromEntity)
                        .toList()
        );
    }

    public ChatRoom createNewChatRoom(User initiatedBy, User initiatedTo) {
        ChatRoom newChatRoom = ChatRoom.builder()
                .initiatedBy(initiatedBy)
                .initiatedTo(initiatedTo)
                .userChatRooms(new ArrayList<>())
                .build();

        UserChatRoom userChatRoom1 = createUserChatRoom(initiatedBy, newChatRoom);
        UserChatRoom userChatRoom2 = createUserChatRoom(initiatedTo, newChatRoom);

        newChatRoom.getUserChatRooms().add(userChatRoom1);
        newChatRoom.getUserChatRooms().add(userChatRoom2);

        return chatRoomRepository.save(newChatRoom);
    }

    private UserChatRoom createUserChatRoom(User user, ChatRoom chatRoom) {
        return UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom)
                .build();
    }

    public void leaveChatRoom(User user, Long chatRoomId) {
    }

    public void deleteChatRoom(User user, Long chatRoomId) {
    }

}
