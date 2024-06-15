package com.linked.classbridge.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linked.classbridge.domain.ChatMessage;
import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.UserChatRoom;
import com.linked.classbridge.dto.chat.ChatRoomDto;
import com.linked.classbridge.dto.chat.CreateChatRoom;
import com.linked.classbridge.dto.chat.JoinChatRoom;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.ChatRoomRepository;
import com.linked.classbridge.type.ErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @Mock
    private OneDayClassService oneDayClassService;
    @Mock
    private ChatService chatService;
    private User mockTutor;
    private User mockUser;
    private final Long mockUserId = 2L;
    private OneDayClass mockOneDayClass;
    private final Long mockClassId = 1L;
    private ChatRoom newChatRoom;

    @BeforeEach
    void setUp() {

        Long mockTutorId = 1L;
        mockTutor = User.builder()
                .userId(mockTutorId)
                .email("tutor@mail.com")
                .nickname("tutor")
                .build();

        mockUser = User.builder()
                .userId(mockUserId)
                .email("user@mail.com")
                .nickname("user")
                .build();

        mockOneDayClass = OneDayClass.builder()
                .classId(mockClassId)
                .tutor(mockTutor)
                .build();

        UserChatRoom userChatRoom1 = UserChatRoom.builder()
                .userChatRoomId(1L)
                .user(mockUser)
                .chatRoom(newChatRoom)
                .build();

        UserChatRoom userChatRoom2 = UserChatRoom.builder()
                .userChatRoomId(2L)
                .user(mockTutor)
                .chatRoom(newChatRoom)
                .build();

        newChatRoom = ChatRoom.builder()
                .initiatedBy(mockUser)
                .initiatedTo(mockTutor)
                .userChatRooms(new ArrayList<>())
                .build();
        newChatRoom.getUserChatRooms().add(userChatRoom1);
        newChatRoom.getUserChatRooms().add(userChatRoom2);
    }

    @Test
    @DisplayName("채팅방이 존재하지 않을 경우 생성")
    void createOrGetChatRoom_shouldCreateNewChatRoom() {
        // given

        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomId(1L)
                .initiatedBy(mockUser)
                .initiatedTo(mockTutor)
                .userChatRooms(new ArrayList<>())
                .build();

        when(oneDayClassService.findClassById(mockClassId)).thenReturn(mockOneDayClass);
        when(chatRoomRepository.findByInitiatedByAndInitiatedTo(mockUser, mockTutor)).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        // when
        CreateChatRoom.Response response = chatRoomService.createOrGetChatRoom(mockUser, mockClassId);

        // then
        Assertions.assertEquals(response.chatRoomId(), 1L);
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("기존 채팅방이 존재할 경우 채팅방을 반환")
    void createOrGetChatRoom_shouldReturnExistingChatRoom() {
        // given
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomId(1L)
                .initiatedBy(mockUser)
                .initiatedTo(mockTutor)
                .userChatRooms(new ArrayList<>())
                .build();

        when(oneDayClassService.findClassById(mockClassId)).thenReturn(mockOneDayClass);
        when(chatRoomRepository.findByInitiatedByAndInitiatedTo(mockUser, mockTutor)).thenReturn(Optional.of(chatRoom));

        // when
        CreateChatRoom.Response response = chatRoomService.createOrGetChatRoom(mockUser, mockClassId);

        // then
        Assertions.assertEquals(response.chatRoomId(), 1L);
        verify(chatRoomRepository, times(0)).save(any(ChatRoom.class));

    }

    @Test
    @DisplayName("채팅방 생성 실패 - 나의 클래스일 경우")
    void createOrGetChatRoom_shouldThrowBadRequestException() {
        // given
        when(oneDayClassService.findClassById(mockClassId)).thenReturn(mockOneDayClass);

        // when & then
        RestApiException exception = Assertions.assertThrows(RestApiException.class, () -> {
            chatRoomService.createOrGetChatRoom(mockTutor, mockClassId);
        });

        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.BAD_REQUEST);
    }

    @Test
    @DisplayName("채팅방 참가 & 메시지 조회 성공")
    void joinChatRoomAndGetMessages_success() {
        // given
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomId(1L)
                .initiatedBy(mockUser)
                .initiatedTo(mockTutor)
                .userChatRooms(new ArrayList<>())
                .build();

        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(ChatMessage.builder()
                .id("1")
                .chatRoomId(chatRoom.getChatRoomId())
                .senderId(mockUserId)
                .message("message")
                .isRead(false)
                .sendTime(LocalDateTime.now())
                .build());

        when(chatRoomRepository.findByChatRoomId(1L)).thenReturn(Optional.of(chatRoom));
        when(chatService.getChatMessagesAndMarkAsRead(1L, mockUserId)).thenReturn(chatMessages);

        // when
        JoinChatRoom.Response response = chatRoomService.joinChatRoomAndGetMessages(mockUser, 1L);

        // then
        Assertions.assertEquals(response.chatRoomId(), 1L);
        verify(chatService, times(1)).getChatMessagesAndMarkAsRead(1L, mockUserId);
    }

    @Test
    @DisplayName("채팅방 참가 실패 - 채팅방에 멤버가 아닐 때")
    void joinChatRoomAndGetMessages_shouldThrowBadRequestException_whenNotMemberOfChatRoom() {
        // given
        User anotherUser = User.builder()
                .userId(3L)
                .build();

        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomId(1L)
                .initiatedBy(mockUser)
                .initiatedTo(mockTutor)
                .userChatRooms(new ArrayList<>())
                .build();

        when(chatRoomRepository.findByChatRoomId(1L)).thenReturn(Optional.of(chatRoom));

        // when & then
        RestApiException exception = Assertions.assertThrows(RestApiException.class, () -> {
            chatRoomService.joinChatRoomAndGetMessages(anotherUser, 1L);
        });

        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.USER_NOT_IN_CHAT_ROOM);
    }

    @Test
    @DisplayName("채팅방 참가 실패 - 채팅방이 존재하지 않을 때")
    void joinChatRoomAndGetMessages_shouldThrowBadRequestException_whenChatRoomNotExist() {
        // given

        when(chatRoomRepository.findByChatRoomId(1L)).thenReturn(Optional.empty());

        // when & then
        RestApiException exception = Assertions.assertThrows(RestApiException.class, () -> {
            chatRoomService.joinChatRoomAndGetMessages(mockUser, 1L);
        });

        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.CHAT_ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("채팅방 목록 조회")
    void getChatRooms_shouldReturnChatRooms() {
        // given
        List<ChatRoom> chatRooms = new ArrayList<>();
        chatRooms.add(ChatRoom.builder()
                .chatRoomId(1L)
                .initiatedBy(mockUser)
                .initiatedTo(mockTutor)
                .userChatRooms(new ArrayList<>())
                .build());
        chatRooms.add(ChatRoom.builder()
                .chatRoomId(2L)
                .initiatedBy(mockTutor)
                .initiatedTo(mockUser)
                .userChatRooms(new ArrayList<>())
                .build());

        when(chatRoomRepository.findAllByUserOrderByUpdatedAtDesc(mockUser)).thenReturn(chatRooms);

        // when
        ChatRoomDto responses = chatRoomService.getChatRooms(mockUser);

        // then
        Assertions.assertEquals(responses.getInquiredChatRoomsChatRooms().size(), 1);
        Assertions.assertEquals(responses.getInquiredChatRoomsChatRooms().get(0).getChatRoomId(), 1L);
        Assertions.assertEquals(responses.getReceivedInquiryChatRoomsChatRooms().size(), 1);
        Assertions.assertEquals(responses.getReceivedInquiryChatRoomsChatRooms().get(0).getChatRoomId(), 2L);
    }

}