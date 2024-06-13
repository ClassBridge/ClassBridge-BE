package com.linked.classbridge.service;

import com.linked.classbridge.domain.ChatMessage;
import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.UserChatRoom;
import com.linked.classbridge.dto.chat.ChatMessageDto;
import com.linked.classbridge.dto.chat.ChatRoomDto;
import com.linked.classbridge.dto.chat.CreateChatRoom;
import com.linked.classbridge.dto.chat.JoinChatRoom;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.ChatRoomRepository;
import com.linked.classbridge.type.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    private final OneDayClassService oneDayClassService;

    private final ChatService chatService;

    @Transactional
    public CreateChatRoom.Response createOrGetChatRoom(User initiatedBy, Long classId) {
        User initiatedTo = oneDayClassService.findClassById(classId).getTutor();

        if (initiatedBy.getUserId().equals(initiatedTo.getUserId())) {
            throw new RestApiException(ErrorCode.BAD_REQUEST);
        }

        // 이미 채팅방이 존재하는지 확인
        ChatRoom chatRoom
                = chatRoomRepository.findByInitiatedByAndInitiatedTo(initiatedBy, initiatedTo)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = ChatRoom.builder()
                            .initiatedBy(initiatedBy)
                            .initiatedTo(initiatedTo)
                            .userChatRooms(new ArrayList<>())
                            .build();
                    UserChatRoom userChatRoom1 = UserChatRoom.builder()
                            .user(initiatedBy)
                            .chatRoom(newChatRoom)
                            .build();
                    UserChatRoom userChatRoom2 = UserChatRoom.builder()
                            .user(initiatedTo)
                            .chatRoom(newChatRoom)
                            .build();
                    newChatRoom.getUserChatRooms().add(userChatRoom1);
                    newChatRoom.getUserChatRooms().add(userChatRoom2);
                    return chatRoomRepository.save(newChatRoom);
                });

        return CreateChatRoom.Response.fromEntity(chatRoom);
    }

    @Transactional
    public JoinChatRoom.Response joinChatRoomAndGetMessages(User user, Long chatRoomId) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);

        // 내가 생성하거나 초대된 채팅방이 아닌 경우
        if (!chatRoom.getInitiatedBy().getUserId().equals(user.getUserId())
                && !chatRoom.getInitiatedTo().getUserId().equals(user.getUserId())) {
            throw new RestApiException(ErrorCode.BAD_REQUEST);
        }

        // 채팅방 메시지 조회
        List<ChatMessage> chatMessages = chatService.findLatestChatMessagesByChatRoom(chatRoomId);

        // 메시지 읽음 처리
        chatMessages.stream()
                .filter(chatMessage -> !chatMessage.isRead())
                .filter(chatMessage -> !chatMessage.getSenderId().equals(user.getUserId()))
                .forEach(chatService::markAsRead);

        // 나를 온라인 상태로 변경
        // TODO : 추후 redis로 관리
        chatRoom.getUserChatRooms().stream()
                .filter(userChatRoom -> userChatRoom.getUser().getUserId().equals(user.getUserId()))
                .findFirst()
                .ifPresent(UserChatRoom::toggleOnline);

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

    public ChatRoom findChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RestApiException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    public ChatRoomDto getChatRooms(User user) {
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserOrderByUpdatedAtDesc(user);
        ChatRoomDto chatRoomDto = new ChatRoomDto();
        for (ChatRoom chatRoom : chatRooms) {
            if (chatRoom.getInitiatedBy().getUserId().equals(user.getUserId())) {
                chatRoomDto.addInquiredChatRoom(chatRoom);
            } else {
                chatRoomDto.addReceivedInquiryChatRoom(chatRoom);
            }
        }
        return chatRoomDto;
    }
}
