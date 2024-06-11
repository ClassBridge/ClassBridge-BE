package com.linked.classbridge.service;

import com.linked.classbridge.domain.ChatMessage;
import com.linked.classbridge.repository.ChatMessageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatMessage saveMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getMessages() {
        return chatMessageRepository.findAll();
    }

    public List<ChatMessage> getMessagesByRoomId(Long roomId) {
        return null;
    }
}
