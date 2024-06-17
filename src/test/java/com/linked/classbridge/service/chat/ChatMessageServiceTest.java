package com.linked.classbridge.service.chat;

import static com.linked.classbridge.type.ErrorCode.CHAT_MESSAGE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linked.classbridge.domain.ChatMessage;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.ChatMessageRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {
    @InjectMocks
    private ChatMessageService chatService;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    private ChatMessage chatMessage;
    private ChatMessage chatMessage2;
    private Long chatRoomId;
    private Long userId;

    @BeforeEach
    void setUp() {
        chatRoomId = 1L;
        userId = 1L;
        chatMessage = ChatMessage.builder()
                .id("1")
                .chatRoomId(chatRoomId)
                .senderId(userId)
                .isRead(false)
                .build();

        chatMessage2 = ChatMessage.builder()
                .id("2")
                .chatRoomId(chatRoomId)
                .senderId(2L)
                .isRead(false)
                .build();
    }

    @Test
    void saveMessage() {
        // given
        when(chatMessageRepository.save(chatMessage)).thenReturn(chatMessage);

        // when
        ChatMessage savedChatMessage = chatService.saveMessage(chatMessage);

        // then
        verify(chatMessageRepository, times(1)).save(chatMessage);
        assertEquals(chatMessage.getMessage(), savedChatMessage.getMessage());
    }

    @Test
    void findLatestChatMessagesByChatRoom() {
        // given
        when(chatMessageRepository.findByChatRoomIdOrderBySendTimeAsc(chatRoomId)).thenReturn(List.of(chatMessage));

        // when
        List<ChatMessage> result = chatService.findLatestChatMessagesByChatRoom(chatRoomId);

        // then
        verify(chatMessageRepository, times(1)).findByChatRoomIdOrderBySendTimeAsc(chatRoomId);
        assertEquals(1, result.size());
    }

    @Test
    void findUnreadMessages() {
        // given
        when(chatMessageRepository.findByChatRoomIdAndSenderIdNotAndIsReadFalse(chatRoomId, userId))
                .thenReturn(List.of(chatMessage));

        // when
        List<ChatMessage> result = chatService.findMessagesUserNotRead(chatRoomId, userId);

        // then
        verify(chatMessageRepository, times(1))
                .findByChatRoomIdAndSenderIdNotAndIsReadFalse(chatRoomId, userId);
        assertEquals(1, result.size());
    }

    @Test
    void findChatMessageById() {
        // given
        when(chatMessageRepository.findById(chatMessage.getId())).thenReturn(java.util.Optional.of(chatMessage));

        // when
        ChatMessage result = chatService.findChatMessageById(chatMessage.getId());

        // then
        verify(chatMessageRepository, times(1)).findById(chatMessage.getId());
        assertEquals(chatMessage.getId(), result.getId());
    }

    @Test
    void findChatMessageByIdNotFound() {
        // given
        when(chatMessageRepository.findById(chatMessage.getId())).thenReturn(java.util.Optional.empty());

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> chatService.findChatMessageById(chatMessage.getId()));

        // then
        verify(chatMessageRepository, times(1)).findById(chatMessage.getId());
        assertEquals(CHAT_MESSAGE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void findAllMessageByChatRoomIdOrderBySendTimeDesc() {
        // given
        when(chatMessageRepository.findByChatRoomIdOrderBySendTimeDesc(chatRoomId)).thenReturn(List.of(chatMessage));

        // when
        ChatMessage result = chatService.findAllMessageByChatRoomIdOrderBySendTimeDesc(chatRoomId);

        // then
        verify(chatMessageRepository, times(1)).findByChatRoomIdOrderBySendTimeDesc(chatRoomId);
        assertEquals(chatMessage.getId(), result.getId());
    }

    @Test
    void markAsReadAndSave() {
        // when
        chatService.markAsReadAndSave(chatMessage);

        // then
        assertTrue(chatMessage.isRead());
        verify(chatMessageRepository, times(1)).save(chatMessage);
    }

    @Test
    void markSentByOtherChatMessagesAsRead() {
        // given
        List<ChatMessage> chatMessages = List.of(chatMessage, chatMessage2);

        // when
        List<ChatMessage> result = chatService.markSentByOtherChatMessagesAsRead(chatMessages, userId);

        // then
        assertFalse(chatMessage.isRead());
        assertTrue(chatMessage2.isRead());
        verify(chatMessageRepository, times(1)).save(chatMessage2);
        assertEquals(1, result.size());
    }
}