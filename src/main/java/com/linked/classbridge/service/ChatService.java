package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.CHAT_ROOM_NOT_FOUND;
import static com.linked.classbridge.type.ErrorCode.USER_NOT_FOUND;

import com.linked.classbridge.domain.ChatMessage;
import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.chat.ChatMessageDto;
import com.linked.classbridge.dto.chat.ReadReceipt;
import com.linked.classbridge.dto.chat.ReadReceiptList;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.ChatMessageRepository;
import com.linked.classbridge.repository.ChatRoomRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;

    private final ChatRoomRepository chatRoomRepository;

    private final ChatMessageRepository chatMessageRepository;

    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatMessage saveMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    /**
     * 채팅방 입장 시, 채팅방의 메시지를 가져오고 읽음 표시를 보낸다.
     *
     * @param chatRoomId 메시지를 가져올 채팅방
     * @param userId     읽음 표시를 보낼 유저
     * @return 채팅방의 메시지 리스트
     */
    public List<ChatMessage> getChatMessagesAndMarkAsRead(Long chatRoomId, Long userId) {
        List<ChatMessage> chatMessages = findLatestChatMessagesByChatRoom(chatRoomId);

        List<ChatMessage> unreadMessageListToSendReceipt = chatMessages.stream()
                .filter(chatMessage -> !chatMessage.isRead())
                .filter(chatMessage -> !chatMessage.getSenderId().equals(userId))
                .toList();

        markMessagesAsRead(unreadMessageListToSendReceipt);

        ReadReceiptList readReceiptList = new ReadReceiptList(
                unreadMessageListToSendReceipt.stream()
                        .map(chatMessage -> new ReadReceipt(chatMessage.getId(), userId))
                        .toList()
        );

        sendReadReceipt(chatRoomId, readReceiptList);

        return chatMessages;
    }

    /**
     * 채팅방의 최신 메시지를 가져온다.
     *
     * @param chatRoomId 채팅방 아이디
     * @return 채팅방의 최신 메시지 리스트
     */
    public List<ChatMessage> findLatestChatMessagesByChatRoom(Long chatRoomId) {
        return chatMessageRepository.findByChatRoomIdOrderBySendTimeAsc(chatRoomId);
    }

    /**
     * 메시지를 저장하고, 채팅방에 메시지를 보낸다.
     *
     * @param senderEmail 메시지를 보낼 유저
     * @param chatRoomId  메시지를 보낼 채팅방
     * @param message     보낼 메시지
     */
    public void sendMessage(String senderEmail, Long chatRoomId, String message) {

        User user = getUserByEmail(senderEmail);
        ChatRoom chatRoom = getChatRoomById(chatRoomId);

        ChatMessage savedChatMessage = saveMessage(
                ChatMessage.builder()
                        .senderId(user.getUserId())
                        .chatRoomId(chatRoom.getChatRoomId())
                        .message(message)
                        .sendTime(LocalDateTime.now())
                        .isRead(false)
                        .build()
        );

        simpMessagingTemplate.convertAndSend("/chatRoom/" + chatRoom.getChatRoomId(),
                ChatMessageDto.fromEntity(savedChatMessage));
    }

    /**
     * 상대의 메시지를 읽었을 때, 메시지를 읽음 처리하고 읽음 표시를 보낸다.
     *
     * @param userEmail 읽음 처리할 유저
     * @param messageId 읽음 처리할 메시지
     */
    public void markMessageAsReadAndSendReceipt(String userEmail, String messageId) {
        User user = getUserByEmail(userEmail);
        ChatMessage chatMessage = getChatMessageById(messageId);

        ChatRoom chatRoom = getChatRoomById(chatMessage.getChatRoomId());

        if (user.getUserId().equals(chatMessage.getSenderId())) {
            throw new RestApiException(ErrorCode.SENDER_CANNOT_MARK_AS_READ);
        }

        validateUserInChatRoom(user, chatRoom);

        markAsReadAndSave(chatMessage);

        ReadReceiptList readReceiptList = new ReadReceiptList(
                List.of(new ReadReceipt(chatMessage.getId(), user.getUserId()))
        );

        sendReadReceipt(chatRoom.getChatRoomId(), readReceiptList);
    }

    public void markAsReadAndSave(ChatMessage chatMessage) {
        chatMessage.readMessage();
        saveMessage(chatMessage);
    }

    public void markMessagesAsRead(List<ChatMessage> messages) {
        messages.forEach(this::markAsReadAndSave);
    }


    public void sendReadReceipt(Long chatRoomId, ReadReceiptList readReceiptList) {
        simpMessagingTemplate.convertAndSend("/read/" + chatRoomId, readReceiptList);
    }

    private ChatRoom getChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RestApiException(CHAT_ROOM_NOT_FOUND));
    }

    private ChatMessage getChatMessageById(String messageId) {
        return chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RestApiException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));
    }

    private User getUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
    }

    private void validateUserInChatRoom(User user, ChatRoom chatRoom) {
        if (!chatRoom.getInitiatedBy().getUserId().equals(user.getUserId())
                && !chatRoom.getInitiatedTo().getUserId().equals(user.getUserId())) {
            throw new RestApiException(ErrorCode.USER_NOT_IN_CHAT_ROOM);
        }
    }

}
