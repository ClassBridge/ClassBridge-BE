package com.linked.classbridge.service;

import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.UserChatRoom;
import com.linked.classbridge.dto.chat.CreateChatRoom;
import com.linked.classbridge.dto.chat.CreateChatRoom.Response;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.ChatRoomRepository;
import com.linked.classbridge.type.ErrorCode;
import java.util.ArrayList;
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

    public Response joinChatRoom(User user, Long chatRoomId) {
        return null;
    }
}
