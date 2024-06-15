package com.linked.classbridge.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;

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
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @InjectMocks
    private ChatService chatService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private
    SimpMessagingTemplate simpMessagingTemplate;


    @Test
    @DisplayName("메시지 저장 테스트")
    void saveMessage() {
        // given
        ChatMessage chatMessage = ChatMessage.builder()
                .senderId(1L)
                .chatRoomId(1L)
                .message("message")
                .build();
        given(chatMessageRepository.save(chatMessage)).willReturn(chatMessage);

        // when
        ChatMessage savedChatMessage = chatService.saveMessage(chatMessage);

        // then
        Mockito.verify(chatMessageRepository, times(1)).save(chatMessage);
        Assertions.assertEquals(chatMessage.getMessage(), savedChatMessage.getMessage());
    }

    @Test
    @DisplayName("채팅방 아이디로 최신 메시지 조회")
    void findLatestChatMessagesByChatRoom() {
        // given
        Long chatRoomId = 1L;

        List<ChatMessage> chatMessages = List.of(
                ChatMessage.builder()
                        .senderId(1L)
                        .chatRoomId(1L)
                        .message("message1")
                        .sendTime(null)
                        .isRead(false)
                        .build(),
                ChatMessage.builder()
                        .senderId(1L)
                        .chatRoomId(1L)
                        .message("message2")
                        .sendTime(null)
                        .isRead(false)
                        .build()
        );

        given(chatMessageRepository.findByChatRoomIdOrderBySendTimeAsc(chatRoomId)).willReturn(chatMessages);
        // when
        List<ChatMessage> result = chatService.findLatestChatMessagesByChatRoom(chatRoomId);

        // then
        Mockito.verify(chatMessageRepository, times(1)).findByChatRoomIdOrderBySendTimeAsc(chatRoomId);
        Assertions.assertEquals(chatMessages.size(), result.size());

    }

    @Test
    @DisplayName("메시지 읽음 처리")
    void markAsRead() {
        // given
        ChatMessage chatMessage = ChatMessage.builder()
                .senderId(1L)
                .chatRoomId(1L)
                .message("message")
                .isRead(false)
                .build();
        given(chatMessageRepository.save(chatMessage)).willReturn(chatMessage);

        // when
        chatService.markAsReadAndSave(chatMessage);

        // then
        Mockito.verify(chatMessageRepository, times(1)).save(chatMessage);
        Assertions.assertTrue(chatMessage.isRead());
    }

    @Test
    @DisplayName("메시지 전송")
    void sendMessage() {
        // given
        String senderEmail = "user@mail.com";
        Long chatRoomId = 1L;
        String message = "message";

        User user = User.builder()
                .userId(1L)
                .email(senderEmail)
                .build();

        User tutor = User.builder()
                .userId(2L)
                .email("tutor@mail.com")
                .build();

        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomId(chatRoomId)
                .initiatedBy(user)
                .initiatedTo(tutor)
                .userChatRooms(List.of())
                .build();

        ChatMessage chatMessage = ChatMessage.builder()
                .senderId(user.getUserId())
                .chatRoomId(chatRoom.getChatRoomId())
                .message(message)
                .sendTime(LocalDateTime.now())
                .isRead(false)
                .build();

        given(userRepository.findByEmail(senderEmail)).willReturn(Optional.of(user));
        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(chatMessage);

        // when
        chatService.sendMessage(senderEmail, chatRoomId, message);

        // then
        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        Mockito.verify(chatMessageRepository, times(1)).save(captor.capture());
        ChatMessage savedChatMessage = captor.getValue();
        savedChatMessage.setSendTime(chatMessage.getSendTime());
        Assertions.assertEquals(user.getUserId(), savedChatMessage.getSenderId());
        Assertions.assertEquals(chatRoom.getChatRoomId(), savedChatMessage.getChatRoomId());
        Assertions.assertEquals(message, savedChatMessage.getMessage());

        Mockito.verify(simpMessagingTemplate, times(1))
                .convertAndSend("/chatRoom/" + chatRoom.getChatRoomId(), ChatMessageDto.fromEntity(savedChatMessage));

    }

    @Test
    @DisplayName("메시지 읽음 처리 및 읽음 표시 전송")
    void markAsReadAndSendReceipt() {
        // given
        User user = User.builder()
                .userId(1L)
                .email("user@mail.com")
                .build();

        ChatMessage chatMessage = ChatMessage.builder()
                .id("1")
                .senderId(2L)
                .chatRoomId(1L)
                .message("message")
                .isRead(false)
                .sendTime(LocalDateTime.now())
                .build();

        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomId(1L)
                .initiatedBy(user)
                .initiatedTo(User.builder().userId(2L).build())
                .userChatRooms(List.of())
                .build();

        ReadReceiptList readReceiptList = new ReadReceiptList(
                List.of(new ReadReceipt(chatMessage.getId(), user.getUserId()))
        );

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(chatRoomRepository.findById(chatMessage.getChatRoomId())).willReturn(
                Optional.of(chatRoom));
        given(chatMessageRepository.findById(chatMessage.getId())).willReturn(Optional.of(chatMessage));
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(chatMessage);

        // when
        chatService.markMessageAsReadAndSendReceipt(user.getEmail(), chatMessage.getId());

        // then
        ArgumentCaptor<ChatMessage> chatMessageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        ArgumentCaptor<ReadReceiptList> readReceiptCaptor = ArgumentCaptor.forClass(ReadReceiptList.class);

        Mockito.verify(chatMessageRepository, times(1)).save(chatMessageCaptor.capture());
        ChatMessage savedChatMessage = chatMessageCaptor.getValue();
        savedChatMessage.setSendTime(chatMessage.getSendTime());

        Assertions.assertTrue(savedChatMessage.isRead());

        Mockito.verify(simpMessagingTemplate, times(1))
                .convertAndSend(eq("/read/" + chatRoom.getChatRoomId()), readReceiptCaptor.capture());
        ReadReceiptList capturedReadReceiptList = readReceiptCaptor.getValue();

        Assertions.assertEquals(readReceiptList, capturedReadReceiptList);
    }

    @Test
    @DisplayName("메시지 읽음 처리 및 읽음 표시 전송 실패 - 메시지를 보낸 유저와 읽음 처리 할 유저가 같음")
    void markAsReadAndSendReceiptFail() {
        // given
        User user = User.builder()
                .userId(1L)
                .email("user@mail.com")
                .build();
        ChatMessage chatMessage = ChatMessage.builder()
                .id("1")
                .senderId(1L)
                .chatRoomId(1L)
                .message("message")
                .isRead(false)
                .sendTime(LocalDateTime.now())
                .build();

        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomId(1L)
                .initiatedBy(user)
                .initiatedTo(User.builder().userId(2L).build())
                .userChatRooms(List.of())
                .build();

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(chatMessageRepository.findById(chatMessage.getId())).willReturn(Optional.of(chatMessage));
        given(chatRoomRepository.findById(chatMessage.getChatRoomId())).willReturn(
                Optional.of(chatRoom));

        // when
        RestApiException exception = Assertions.assertThrows(RestApiException.class, () -> {
            chatService.markMessageAsReadAndSendReceipt(user.getEmail(), chatMessage.getId());
        });

        // then
        Mockito.verify(chatMessageRepository, times(0)).save(any());
        Assertions.assertFalse(chatMessage.isRead());
        Assertions.assertEquals(ErrorCode.SENDER_CANNOT_MARK_AS_READ, exception.getErrorCode());
    }

}