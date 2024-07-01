package com.linked.classbridge.service.chat;

import static com.linked.classbridge.type.ErrorCode.BAD_REQUEST;
import static com.linked.classbridge.type.ErrorCode.SENDER_CANNOT_MARK_AS_READ;
import static com.linked.classbridge.type.ErrorCode.USER_NOT_IN_CHAT_ROOM;

import com.linked.classbridge.domain.ChatMessage;
import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.UserChatRoom;
import com.linked.classbridge.dto.chat.ChatMessageDto;
import com.linked.classbridge.dto.chat.ChatRoomUnreadCountInfoDto;
import com.linked.classbridge.dto.chat.CreateChatRoom;
import com.linked.classbridge.dto.chat.GetChatRoomsResponse;
import com.linked.classbridge.dto.chat.JoinChatRoom;
import com.linked.classbridge.dto.chat.ReadReceipt;
import com.linked.classbridge.dto.chat.ReadReceiptList;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final UserService userService;

    private final ChatRoomService chatRoomService;

    private final ChatMessageService chatMessageService;

    private final UserChatRoomService userChatRoomService;

    private final MessageSendingService messageSendingService;

    // 채팅방 생성
    public CreateChatRoom.Response createChatRoomProcess(User chatStartUser, Long chatPartnerId) {
        log.info("Create chat room initiated by user: {}", chatStartUser.getUserId());
        User chatPartner = userService.findUserById(chatPartnerId);

        validateChatRoomInitiation(chatStartUser, chatPartner);

        ChatRoom chatRoom = chatRoomService.createOrFindChatRoomByUsers(chatStartUser, chatPartner);

        log.info("Chat room created: {}", chatRoom.getChatRoomId());
        return CreateChatRoom.Response.fromEntity(chatRoom);
    }

    // 채팅방 입장
    public JoinChatRoom.Response enterChatRoomProcess(User user, Long chatRoomId) {
        log.info("User {} is entering chat room {}", user.getUserId(), chatRoomId);
        ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

        validateUserInChatRoom(user, chatRoom);

        List<ChatMessage> chatMessages = chatMessageService.findLatestChatMessagesByChatRoom(chatRoomId);

        List<ChatMessage> markedMessages = chatMessageService.markSentByOtherChatMessagesAsRead(chatMessages,
                user.getUserId());

        ReadReceiptList receiptList = ReadReceiptList.builder()
                .readReceipts(markedMessages.stream()
                        .map(chatMessage -> new ReadReceipt(chatMessage.getId(), user.getUserId()))
                        .toList())
                .build();

        messageSendingService.sendReadReceipt(chatRoomId, receiptList);

        // TODO : 추후 redis로 관리
        userChatRoomService.setUserToOnline(user, chatRoom.getUserChatRooms());

        log.info("User {} entered chat room {}", user.getUserId(), chatRoomId);
        return createJoinChatRoomResponse(chatRoom, user, chatMessages);
    }

    private JoinChatRoom.Response createJoinChatRoomResponse(ChatRoom chatRoom, User user, List<ChatMessage> messages) {
        return JoinChatRoom.Response.of(
                chatRoom.getChatRoomId(),
                user.getUserId(),
                chatRoom.getInitiatedBy().getUserId(),
                chatRoom.getInitiatedTo().getUserId(),
                chatRoom.getCreatedAt(),
                chatRoom.getUpdatedAt(),
                messages.stream()
                        .map(ChatMessageDto::fromEntity)
                        .toList()
        );
    }

    // 채팅방 목록 조회
    public GetChatRoomsResponse getChatRoomListProcess(User user) {
        List<ChatRoom> chatRooms = chatRoomService.findAllChatRoomsByUser(user);
        GetChatRoomsResponse getChatRoomsResponse = new GetChatRoomsResponse();
        getChatRoomsResponse.setUserId(user.getUserId());

        chatRooms.forEach(chatRoom -> {
            ChatRoomUnreadCountInfoDto unreadCountInfo = createChatRoomUnreadCountInfoDto(chatRoom, user);
            User chatPartner = chatRoom.getInitiatedBy().getUserId().equals(user.getUserId())
                    ? chatRoom.getInitiatedTo()
                    : chatRoom.getInitiatedBy();
            getChatRoomsResponse.addChatRoomInfo(chatRoom, chatPartner, unreadCountInfo);
        });

        return getChatRoomsResponse;
    }

    private ChatRoomUnreadCountInfoDto createChatRoomUnreadCountInfoDto(ChatRoom chatRoom, User user) {
        Long chatRoomId = chatRoom.getChatRoomId();
        ChatMessage latestMessage = chatMessageService.findAllMessageByChatRoomIdOrderBySendTimeDesc(chatRoomId);
        int unreadMessageCount = chatMessageService.findMessagesUserNotRead(chatRoomId, user.getUserId()).size();

        return ChatRoomUnreadCountInfoDto.builder()
                .chatRoomId(chatRoomId)
                .unreadMessageCount(unreadMessageCount)
                .latestMessage(latestMessage == null ? "" : latestMessage.getMessage())
                .latestMessageTime(latestMessage == null ? null : latestMessage.getSendTime())
                .build();
    }

    // 채팅방 닫기
    public void closeChatRoomProcess(User user, Long chatRoomId) {
        log.info("User {} is closing chat room {}", user.getUserId(), chatRoomId);
        ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

        log.info("Chat room {} is closed", chatRoom.getChatRoomId());
        userChatRoomService.setUserToOffline(user, chatRoom.getUserChatRooms());
    }

    // 채팅방 퇴장
    @Transactional
    public void leaveChatRoomProcess(User user, Long chatRoomId) {
        log.info("User {} is leaving chat room {}", user.getUserId(), chatRoomId);
        ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

        List<UserChatRoom> userChatRooms = chatRoom.getUserChatRooms();
        UserChatRoom userChatRoom = userChatRooms.stream()
                .filter(ucr -> ucr.getUser().getUserId().equals(user.getUserId()))
                .findFirst()
                .orElseThrow(() -> new RestApiException(USER_NOT_IN_CHAT_ROOM));

        userChatRoomService.setUserToOffline(user, userChatRooms);

        UserChatRoom deletedChatRoom = userChatRoomService.deleteUserChatRoom(userChatRoom);

        userChatRooms.remove(deletedChatRoom);

        if (userChatRooms.isEmpty()) {
            log.info("Chat room {} is empty, deleting chat room", chatRoom.getChatRoomId());
            chatRoomService.deleteChatRoom(chatRoom);
        }

        log.info("User {} left chat room {}", user.getUserId(), chatRoomId);
    }

    // 메시지 전송
    @Transactional
    public void sendMessageProcess(String senderEmail, Long chatRoomId, String message) {
        log.info("User {} is sending message to chat room {}", senderEmail, chatRoomId);
        User sender = userService.getUserByEmail(senderEmail);
        ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

        ChatMessage savedChatMessage = saveChatMessage(sender, chatRoom, message);

        chatRoom.updateLastMessageAt(savedChatMessage.getSendTime());

        log.info("Message {} saved to chat room {}", savedChatMessage.getChatRoomId(), chatRoom.getChatRoomId());

        ChatRoomUnreadCountInfoDto unreadCountInfo = createUnreadCountInfo(chatRoom, savedChatMessage);

        updateUnreadCounts(chatRoom, sender.getUserId(), unreadCountInfo);

        log.info("Message sent to chat room {}", chatRoom.getChatRoomId());
        messageSendingService.broadcastNewMessage(chatRoom.getChatRoomId(), savedChatMessage);
    }

    public ChatMessage saveChatMessage(User sender, ChatRoom chatRoom, String message) {
        return chatMessageService.saveMessage(ChatMessage.builder()
                .senderId(sender.getUserId())
                .chatRoomId(chatRoom.getChatRoomId())
                .message(message)
                .sendTime(LocalDateTime.now())
                .isRead(false)
                .build());
    }

    public ChatRoomUnreadCountInfoDto createUnreadCountInfo(ChatRoom chatRoom, ChatMessage savedChatMessage) {
        return ChatRoomUnreadCountInfoDto.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .latestMessage(savedChatMessage.getMessage())
                .latestMessageTime(savedChatMessage.getSendTime())
                .build();
    }

    private void updateUnreadCounts(ChatRoom chatRoom, Long senderUserId, ChatRoomUnreadCountInfoDto unreadCountInfo) {
        List<UserChatRoom> userChatRooms = userChatRoomService.findAllUserChatRoomsByChatRoomIncludingDeleted(chatRoom);
        log.info("Found {} user chat rooms for chat room {}", userChatRooms.size(), chatRoom.getChatRoomId());

        log.info("Start to handle unread count info for chat room {}", chatRoom.getChatRoomId());
        for (UserChatRoom userChatRoom : userChatRooms) {
            handleUserChatRoom(userChatRoom, chatRoom, senderUserId, unreadCountInfo);
        }
    }

    public void handleUserChatRoom(UserChatRoom userChatRoom, ChatRoom chatRoom, Long senderUserId,
                                   ChatRoomUnreadCountInfoDto unreadCountInfo) {
        Long receiverUserId = userChatRoom.getUser().getUserId();

        if (userChatRoom.getDeletedAt() != null) {
            log.info("UserChatroom {} for user {} is restored by user {}", userChatRoom.getUserChatRoomId(),
                    receiverUserId, senderUserId);
            userChatRoom.restoreUserChatRoom();
        }

        int unreadMessageCount = calculateUnreadMessageCount(userChatRoom, chatRoom, senderUserId, receiverUserId);

        unreadCountInfo.setUnreadMessageCount(unreadMessageCount);

        log.info("Sending unread count info to user {}", receiverUserId);
        messageSendingService.sendUnreadCountInfo(receiverUserId, unreadCountInfo);
    }

    public int calculateUnreadMessageCount(UserChatRoom userChatRoom, ChatRoom chatRoom, Long senderUserId,
                                           Long receiverUserId) {
        if (userChatRoom.isOnline() || senderUserId.equals(receiverUserId)) {
            return 0;
        } else {
            return chatMessageService.findMessagesUserNotRead(chatRoom.getChatRoomId(), receiverUserId).size();
        }
    }

    // 메시지 읽음 처리
    public void markMessageAsReadAndSendReceipt(String userEmail, String messageId) {
        User user = userService.getUserByEmail(userEmail);
        ChatMessage chatMessage = chatMessageService.findChatMessageById(messageId);

        ChatRoom chatRoom = chatRoomService.findChatRoomById(chatMessage.getChatRoomId());

        if (user.getUserId().equals(chatMessage.getSenderId())) {
            throw new RestApiException(SENDER_CANNOT_MARK_AS_READ);
        }

        validateUserInChatRoom(user, chatRoom);

        chatMessageService.markAsReadAndSave(chatMessage);

        ReadReceiptList readReceiptList = new ReadReceiptList(
                List.of(new ReadReceipt(chatMessage.getId(), user.getUserId()))
        );

        messageSendingService.sendReadReceipt(chatRoom.getChatRoomId(), readReceiptList);
    }


    private void validateChatRoomInitiation(User initiatedBy, User initiatedTo) {
        if (initiatedBy.getUserId().equals(initiatedTo.getUserId())) {
            throw new RestApiException(BAD_REQUEST);
        }
    }

    private void validateUserInChatRoom(User user, ChatRoom chatRoom) {
        if (!chatRoom.getInitiatedBy().getUserId().equals(user.getUserId())
                && !chatRoom.getInitiatedTo().getUserId().equals(user.getUserId())) {
            throw new RestApiException(USER_NOT_IN_CHAT_ROOM);
        }
    }

}
