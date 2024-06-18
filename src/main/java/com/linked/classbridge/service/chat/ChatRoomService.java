package com.linked.classbridge.service.chat;

import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.UserChatRoom;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.ChatRoomRepository;
import com.linked.classbridge.type.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoom createOrFindChatRoomByUsers(User initiatedBy, User initiatedTo) {
        return chatRoomRepository.findByInitiatedByAndInitiatedTo(initiatedBy, initiatedTo)
                .orElseGet(() -> createNewChatRoom(initiatedBy, initiatedTo));
    }

    public void deleteChatRoom(ChatRoom chatRoom) {
        chatRoomRepository.delete(chatRoom);
    }

    public List<ChatRoom> findAllChatRoomsByUser(User user) {
        return chatRoomRepository.findAllByUserOrderByLastMessageAtDesc(user);
    }

    public ChatRoom findChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new RestApiException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    public ChatRoom createNewChatRoom(User initiatedBy, User initiatedTo) {
        ChatRoom newChatRoom = ChatRoom.builder()
                .initiatedBy(initiatedBy)
                .initiatedTo(initiatedTo)
                .userChatRooms(new ArrayList<>())
                .build();

        UserChatRoom userChatRoom1 = createUserChatRoom(initiatedBy, newChatRoom);
        UserChatRoom userChatRoom2 = createUserChatRoom(initiatedTo, newChatRoom);

        newChatRoom.getUserChatRooms().add(userChatRoom1);
        newChatRoom.getUserChatRooms().add(userChatRoom2);

        return chatRoomRepository.save(newChatRoom);
    }

    private UserChatRoom createUserChatRoom(User user, ChatRoom chatRoom) {
        return UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom)
                .build();
    }

}
