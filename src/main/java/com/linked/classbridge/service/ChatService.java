package com.linked.classbridge.service;

import com.linked.classbridge.domain.ChatMessage;
import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.chat.ChatMessageDto;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final UserRepository userRepository;

    private final ChatRoomRepository chatRoomRepository;

    private final ChatMessageRepository chatMessageRepository;

    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatMessage saveMessage(ChatMessage chatMessage) {
        System.out.println(chatMessage);
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getMessages() {
        return chatMessageRepository.findAll();
    }

    public List<ChatMessage> getMessagesByRoomId(Long roomId) {
        return null;
    }

    public List<ChatMessage> findLatestChatMessagesByChatRoom(Long chatRoomId) {
        return chatMessageRepository.findByChatRoomIdOrderBySendTimeAsc(chatRoomId);
    }

    public void markAsRead(ChatMessage chatMessage) {
        chatMessage.readMessage();
        chatMessageRepository.save(chatMessage);
    }

    public void sendMessage(String senderEmail, Long chatRoomId, String message) {

        User user = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RestApiException(ErrorCode.USER_NOT_FOUND));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RestApiException(ErrorCode.CHAT_ROOM_NOT_FOUND));

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
}
