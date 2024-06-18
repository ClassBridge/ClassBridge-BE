package com.linked.classbridge.service.chat;

import com.linked.classbridge.domain.ChatMessage;
import com.linked.classbridge.dto.chat.ChatMessageDto;
import com.linked.classbridge.dto.chat.ChatRoomUnreadCountInfoDto;
import com.linked.classbridge.dto.chat.ReadReceiptList;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageSendingService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void broadcastNewMessage(Long chatRoomId, ChatMessage newMessage) {
        simpMessagingTemplate.convertAndSend("/chatRoom/" + chatRoomId, ChatMessageDto.fromEntity(newMessage));
    }

    public void sendReadReceipt(Long chatRoomId, ReadReceiptList readReceiptList) {
        simpMessagingTemplate.convertAndSend("/read/" + chatRoomId, readReceiptList);
    }

    public void sendUnreadCountInfo(Long userId, ChatRoomUnreadCountInfoDto unreadCountInfo) {
        simpMessagingTemplate.convertAndSend("/chatRooms/" + userId + "/unreadCountInfo", unreadCountInfo);
    }
}
