package com.linked.classbridge.service.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.UserChatRoom;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.ChatRoomRepository;
import com.linked.classbridge.type.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {
    @InjectMocks
    private ChatRoomService chatRoomService;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    private User user1;
    private User user2;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {

        user1 = User.builder()
                .userId(1L)
                .email("user1@mail.com")
                .build();

        user2 = User.builder()
                .userId(1L)
                .email("user2@mail.com")
                .build();

        chatRoom = ChatRoom.builder()
                .chatRoomId(1L)
                .initiatedBy(user1)
                .initiatedTo(user2)
                .userChatRooms(new ArrayList<>())
                .build();

    }

    @Test
    void createOrFindChatRoomByUsers_CreateNewChatRoom() {
        // given
        when(chatRoomRepository.findByInitiatedByAndInitiatedTo(user1, user2)).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        // when
        ChatRoom response = chatRoomService.createOrFindChatRoomByUsers(user1, user2);

        // then
        assertEquals(response.getChatRoomId(), chatRoom.getChatRoomId());
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
    }

    @Test
    void createOrFindChatRoomByUsers_FindExistingChatRoom() {
        // given
        when(chatRoomRepository.findByInitiatedByAndInitiatedTo(user1, user2)).thenReturn(Optional.of(chatRoom));

        // when
        ChatRoom response = chatRoomService.createOrFindChatRoomByUsers(user1, user2);

        // then
        assertEquals(chatRoom, response);
    }

    @Test
    void deleteChatRoom() {
        // when
        chatRoomService.deleteChatRoom(chatRoom);

        // then
        verify(chatRoomRepository, times(1)).delete(chatRoom);
    }

    @Test
    void findAllChatRoomsByUser() {
        // given
        List<ChatRoom> chatRooms = List.of(chatRoom);

        when(chatRoomRepository.findAllByUserOrderByLastMessageAtDesc(user1)).thenReturn(chatRooms);

        // when
        List<ChatRoom> result = chatRoomService.findAllChatRoomsByUser(user1);

        // then
        assertEquals(result, chatRooms);

    }

    @Test
    void findChatRoomById_Found() {
        // given
        when(chatRoomRepository.findByChatRoomId(1L)).thenReturn(Optional.of(chatRoom));

        // when
        ChatRoom result = chatRoomService.findChatRoomById(1L);

        // then
        assertEquals(result, chatRoom);
    }

    @Test
    void findChatRoomById_NotFound() {
        // given
        when(chatRoomRepository.findByChatRoomId(1L)).thenReturn(Optional.empty());

        // when
        RestApiException exception = Assertions.assertThrows(RestApiException.class, () -> {
            chatRoomService.findChatRoomById(1L);
        });

        // then
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.CHAT_ROOM_NOT_FOUND);
    }

    @Test
    void createNewChatRoom() {
        // given
        UserChatRoom userChatRoom1 = UserChatRoom.builder()
                .user(user1)
                .chatRoom(chatRoom)
                .build();

        UserChatRoom userChatRoom2 = UserChatRoom.builder()
                .user(user2)
                .chatRoom(chatRoom)
                .build();

        chatRoom.getUserChatRooms().add(userChatRoom1);
        chatRoom.getUserChatRooms().add(userChatRoom2);

        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        // when
        ChatRoom result = chatRoomService.createNewChatRoom(user1, user2);

        // then
        assertEquals(result, chatRoom);
        assertEquals(result.getUserChatRooms().size(), 2);
    }

}