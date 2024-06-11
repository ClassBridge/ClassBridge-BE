package com.linked.classbridge.service;

import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.chat.CreateChatRoom;
import com.linked.classbridge.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    private final OneDayClassService oneDayClassService;

    @Transactional
    public CreateChatRoom.Response createOrGetChatRoom(User initiatedBy, Long classId) {
        User initiatedTo = oneDayClassService.findClassById(classId).getTutor();

        // 이미 채팅방이 존재하는지 확인
        ChatRoom chatRoom
                = chatRoomRepository.findByInitiatedByAndInitiatedTo(initiatedBy, initiatedTo)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.builder()
                        .initiatedBy(initiatedBy)
                        .initiatedTo(initiatedTo)
                        .build()));

        return CreateChatRoom.Response.fromEntity(chatRoom);
    }
}
