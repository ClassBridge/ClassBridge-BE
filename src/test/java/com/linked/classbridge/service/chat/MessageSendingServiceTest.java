package com.linked.classbridge.service.chat;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.linked.classbridge.domain.ChatMessage;
import com.linked.classbridge.dto.chat.ChatMessageDto;
import com.linked.classbridge.dto.chat.ChatRoomUnreadCountInfoDto;
import com.linked.classbridge.dto.chat.ReadReceipt;
import com.linked.classbridge.dto.chat.ReadReceiptList;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class MessageSendingServiceTest {

    @InjectMocks
    private MessageSendingService messageSendingService;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    private ChatMessage chatMessage;
    private ReadReceiptList readReceiptList;
    private ChatRoomUnreadCountInfoDto unreadCountInfo;

    @BeforeEach
    void setUp() {
        chatMessage = ChatMessage.builder()
                .id("1")
                .message("Hello")
                .chatRoomId(1L)
                .senderId(1L)
                .sendTime(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build();

        ReadReceipt readReceipt = ReadReceipt.builder()
                .userId(1L)
                .chatMessageId("1")
                .build();

        readReceiptList = ReadReceiptList.builder()
                .readReceipts(new ArrayList<>(List.of(readReceipt)))
                .build();

        unreadCountInfo = ChatRoomUnreadCountInfoDto.builder()
                .chatRoomId(1L)
                .unreadMessageCount(1)
                .latestMessage("Hello")
                .latestMessageTime(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build();
    }

    @Test
    void broadcastNewMessage() {
        messageSendingService.broadcastNewMessage(chatMessage.getChatRoomId(), chatMessage);

        verify(simpMessagingTemplate).convertAndSend(
                eq("/chatRoom/" + chatMessage.getChatRoomId()),
                eq(ChatMessageDto.fromEntity(chatMessage))
        );
    }

    @Test
    void sendReadReceipt() {
        messageSendingService.sendReadReceipt(chatMessage.getChatRoomId(), readReceiptList);

        verify(simpMessagingTemplate).convertAndSend(
                eq("/read/" + chatMessage.getChatRoomId()),
                eq(readReceiptList)
        );
    }

    @Test
    void sendUnreadCountInfo() {
        messageSendingService.sendUnreadCountInfo(chatMessage.getSenderId(), unreadCountInfo);

        verify(simpMessagingTemplate).convertAndSend(
                eq("/chatRooms/" + chatMessage.getSenderId() + "/unreadCountInfo"),
                eq(unreadCountInfo)
        );

    }
}