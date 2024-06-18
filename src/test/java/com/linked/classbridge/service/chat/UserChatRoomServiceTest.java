package com.linked.classbridge.service.chat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.UserChatRoom;
import com.linked.classbridge.repository.UserChatRoomRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserChatRoomServiceTest {
    @InjectMocks
    private UserChatRoomService userChatRoomService;
    @Mock
    private UserChatRoomRepository userChatRoomRepository;
    private User user;
    private ChatRoom chatRoom;
    private UserChatRoom userChatRoom;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .email("user@mail.com")
                .build();

        chatRoom = ChatRoom.builder()
                .chatRoomId(1L)
                .build();

        userChatRoom = UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom)
                .build();
    }

    @Test
    void saveUserChatRoom() {

        userChatRoomService.saveUserChatRoom(userChatRoom);

        verify(userChatRoomRepository).save(userChatRoom);
    }

    @Test
    void deleteUserChatRoom() {

        userChatRoomService.deleteUserChatRoom(userChatRoom);

        verify(userChatRoomRepository).delete(userChatRoom);
    }

    @Test
    void leaveAllChatRoomsByEmail() {

        userChatRoomService.leaveAllChatRoomsByEmail(user);

        verify(userChatRoomRepository).updateIsOnlineByUser_Email(user.getEmail(), false);
    }

    @Test
    void setUserToOnline() {
        // given
        userChatRoom = UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom)
                .isOnline(false)
                .build();

        List<UserChatRoom> userChatRooms = List.of(userChatRoom);

        when(userChatRoomRepository.save(userChatRoom)).thenReturn(userChatRoom);

        // when
        userChatRoomService.setUserToOnline(user, userChatRooms);

        // then
        assertTrue(userChatRoom.isOnline());
        verify(userChatRoomRepository).save(userChatRoom);
    }

    @Test
    void setUSerOffline() {
        // given
        userChatRoom = UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom)
                .isOnline(true)
                .build();

        List<UserChatRoom> userChatRooms = List.of(userChatRoom);

        when(userChatRoomRepository.save(userChatRoom)).thenReturn(userChatRoom);

        // when
        userChatRoomService.setUserToOffline(user, userChatRooms);

        // then
        assertFalse(userChatRoom.isOnline());
        verify(userChatRoomRepository).save(userChatRoom);
    }

}