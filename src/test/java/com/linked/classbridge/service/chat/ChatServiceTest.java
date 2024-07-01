package com.linked.classbridge.service.chat;

import static com.linked.classbridge.type.ErrorCode.USER_NOT_IN_CHAT_ROOM;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linked.classbridge.domain.ChatMessage;
import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.UserChatRoom;
import com.linked.classbridge.dto.chat.ChatRoomUnreadCountInfoDto;
import com.linked.classbridge.dto.chat.CreateChatRoom;
import com.linked.classbridge.dto.chat.GetChatRoomsResponse;
import com.linked.classbridge.dto.chat.JoinChatRoom;
import com.linked.classbridge.dto.chat.ReadReceipt;
import com.linked.classbridge.dto.chat.ReadReceiptList;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.ErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @InjectMocks
    private ChatService chatService;
    @Mock
    private UserService userService;
    @Mock
    private ChatRoomService chatRoomService;
    @Mock
    private ChatMessageService chatMessageService;
    @Mock
    private UserChatRoomService userChatRoomService;
    @Mock
    private MessageSendingService messageSendingService;
    private User user;
    private User tutor;
    private User otherUser;
    private ChatRoom chatRoom1;
    private ChatRoom chatRoom2;
    private UserChatRoom userChatRoom1;
    private UserChatRoom userChatRoom2;
    private ChatMessage chatMessage1;
    private ChatMessage chatMessage2;
    private ChatMessage chatMessage3;

    @BeforeEach
    void setUp() {
        user = User.builder().userId(1L).email("user@mail.com").build();
        tutor = User.builder().userId(2L).email("tutor@mail.com").build();
        otherUser = User.builder().userId(3L).email("other@mail.com").build();
        chatRoom1 = ChatRoom.builder()
                .chatRoomId(1L)
                .initiatedBy(user)
                .initiatedTo(tutor)
                .userChatRooms(new ArrayList<>())
                .build();
        chatRoom2 = ChatRoom.builder()
                .chatRoomId(2L)
                .initiatedBy(otherUser)
                .initiatedTo(user)
                .userChatRooms(new ArrayList<>())
                .build();
        chatMessage1 = ChatMessage.builder()
                .id("1")
                .senderId(user.getUserId())
                .chatRoomId(chatRoom1.getChatRoomId())
                .message("Hello")
                .sendTime(LocalDateTime.of(2024, 1, 1, 0, 0))
                .isRead(true)
                .build();
        chatMessage2 = ChatMessage.builder()
                .id("2")
                .senderId(tutor.getUserId())
                .chatRoomId(chatRoom1.getChatRoomId())
                .message("Hi")
                .sendTime(LocalDateTime.of(2024, 1, 1, 0, 1))
                .isRead(false)
                .build();
        chatMessage3 = ChatMessage.builder()
                .id("3")
                .senderId(otherUser.getUserId())
                .chatRoomId(chatRoom2.getChatRoomId())
                .message("How are you?")
                .sendTime(LocalDateTime.of(2024, 1, 1, 0, 2))
                .isRead(false)
                .build();

        userChatRoom1 = UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom1)
                .isOnline(true)
                .build();
        userChatRoom2 = UserChatRoom.builder()
                .user(tutor)
                .chatRoom(chatRoom1)
                .isOnline(true)
                .build();

        chatRoom1.getUserChatRooms().add(userChatRoom1);
        chatRoom1.getUserChatRooms().add(userChatRoom2);
    }

    @Test
    void createChatRoomProcess() {
        // given
        Long chatPartnerId = 2L;
        given(userService.findUserById(chatPartnerId)).willReturn(tutor);
        given(chatRoomService.createOrFindChatRoomByUsers(user, tutor)).willReturn(chatRoom1);

        // when
        CreateChatRoom.Response createChatRoom = chatService.createChatRoomProcess(user, chatPartnerId);

        // then
        assertEquals(chatRoom1.getChatRoomId(), createChatRoom.chatRoomId());
    }

    @Test
    void createChatRoomProcessInitiatedToMyself() {
        // given
        Long chatPartnerId = 2L;
        given(userService.findUserById(chatPartnerId)).willReturn(tutor);

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> chatService.createChatRoomProcess(tutor, chatPartnerId));

        // then
        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
    }

    @Test
    void enterChatRoomProcess() {
        // given
        Long chatRoomId = 1L;

        List<ChatMessage> chatMessages = new ArrayList<>(List.of(chatMessage1, chatMessage2));
        List<ChatMessage> markedMessages = new ArrayList<>(List.of(chatMessage2));

        ReadReceiptList receiptList = ReadReceiptList.builder()
                .readReceipts(markedMessages.stream()
                        .map(chatMessage -> new ReadReceipt(chatMessage.getId(), user.getUserId()))
                        .toList())
                .build();

        given(chatRoomService.findChatRoomById(chatRoomId)).willReturn(chatRoom1);
        given(chatMessageService.findLatestChatMessagesByChatRoom(chatRoomId)).willReturn(chatMessages);
        given(chatMessageService.markSentByOtherChatMessagesAsRead(chatMessages, user.getUserId()))
                .willReturn(markedMessages);
        doNothing().when(messageSendingService).sendReadReceipt(chatRoomId, receiptList);

        // when
        JoinChatRoom.Response response = chatService.enterChatRoomProcess(user, chatRoomId);

        // then
        assertEquals(chatRoom1.getChatRoomId(), response.chatRoomId());
        assertEquals(user.getUserId(), response.senderId());
        assertEquals(chatRoom1.getInitiatedBy().getUserId(), response.initiatedBy());
    }

    @Test
    void enterChatRoomProcessUserNotInChatRoom() {
        // given
        Long chatRoomId = 1L;
        user = User.builder().userId(3L).build();

        given(chatRoomService.findChatRoomById(chatRoomId)).willReturn(chatRoom1);

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> chatService.enterChatRoomProcess(user, chatRoomId));

        // then
        assertEquals(USER_NOT_IN_CHAT_ROOM, exception.getErrorCode());
    }

    @Test
    void getChatRoomListProcess() {
        // given
        List<ChatRoom> chatRooms = new ArrayList<>(List.of(chatRoom1, chatRoom2));

        given(chatRoomService.findAllChatRoomsByUser(user)).willReturn(chatRooms);

        given(chatMessageService.findAllMessageByChatRoomIdOrderBySendTimeDesc(chatRoom1.getChatRoomId()))
                .willReturn(chatMessage2);
        given(chatMessageService.findMessagesUserNotRead(chatRoom1.getChatRoomId(), user.getUserId()))
                .willReturn(List.of(chatMessage2));

        given(chatMessageService.findAllMessageByChatRoomIdOrderBySendTimeDesc(chatRoom2.getChatRoomId()))
                .willReturn(chatMessage3);
        given(chatMessageService.findMessagesUserNotRead(chatRoom2.getChatRoomId(), user.getUserId()))
                .willReturn(List.of(chatMessage3));

        // when
        GetChatRoomsResponse result = chatService.getChatRoomListProcess(user);

        // then
        assertEquals(user.getUserId(), result.getUserId());
        assertEquals(2, result.getChatRooms().size());
        assertEquals(chatRoom1.getChatRoomId(), result.getChatRooms().get(0).getChatRoomId());
        assertEquals(tutor.getUserId(), result.getChatRooms().get(0).getChatPartnerId());
        assertEquals(chatMessage2.getMessage(), result.getChatRooms().get(0).getUnreadCountInfo().getLatestMessage());
        assertEquals(chatRoom2.getChatRoomId(), result.getChatRooms().get(1).getChatRoomId());
        assertEquals(otherUser.getUserId(), result.getChatRooms().get(1).getChatPartnerId());
        assertEquals(chatMessage3.getMessage(), result.getChatRooms().get(1).getUnreadCountInfo().getLatestMessage());

    }

    @Test
    void closeChatRoomProcess() {
        // given
        given(chatRoomService.findChatRoomById(chatRoom1.getChatRoomId())).willReturn(chatRoom1);
        doNothing().when(userChatRoomService).setUserToOffline(user, chatRoom1.getUserChatRooms());

        // then
        assertDoesNotThrow(() -> chatService.closeChatRoomProcess(user, chatRoom1.getChatRoomId()));
    }

    @Test
    void leaveChatRoomProcess() {
        // given
        List<UserChatRoom> userChatRooms = chatRoom1.getUserChatRooms();
        UserChatRoom userChatRoomToDelete = userChatRooms.get(0);

        when(chatRoomService.findChatRoomById(chatRoom1.getChatRoomId())).thenReturn(chatRoom1);
        doNothing().when(userChatRoomService).setUserToOffline(user, userChatRooms);
        when(userChatRoomService.deleteUserChatRoom(userChatRoomToDelete)).thenReturn(userChatRoomToDelete);

        // when
        chatService.leaveChatRoomProcess(user, chatRoom1.getChatRoomId());

        // then
        verify(userChatRoomService, times(1)).setUserToOffline(user, userChatRooms);
        verify(userChatRoomService, times(1)).deleteUserChatRoom(userChatRoomToDelete);
        verify(chatRoomService, times(0)).deleteChatRoom(chatRoom1);
        assertEquals(1, chatRoom1.getUserChatRooms().size());
    }

    @Test
    void leaveChatRoomProcessDeleteChatRoom() {
        // given
        List<UserChatRoom> userChatRooms = chatRoom1.getUserChatRooms();
        chatRoom1.getUserChatRooms().remove(1);
        UserChatRoom userChatRoomToDelete = userChatRooms.get(0);

        when(chatRoomService.findChatRoomById(chatRoom1.getChatRoomId())).thenReturn(chatRoom1);
        doNothing().when(userChatRoomService).setUserToOffline(user, userChatRooms);
        when(userChatRoomService.deleteUserChatRoom(userChatRoomToDelete)).thenReturn(userChatRoomToDelete);

        // when
        chatService.leaveChatRoomProcess(user, chatRoom1.getChatRoomId());

        // then
        verify(userChatRoomService, times(1)).deleteUserChatRoom(userChatRoomToDelete);
        verify(chatRoomService, times(1)).deleteChatRoom(chatRoom1);
    }

    @Test
    public void testSaveChatMessage() {
        String message = "Hello";

        ChatMessage chatMessage = ChatMessage.builder()
                .senderId(user.getUserId())
                .chatRoomId(chatRoom1.getChatRoomId())
                .message(message)
                .sendTime(LocalDateTime.now())
                .isRead(false)
                .build();

        when(chatMessageService.saveMessage(any(ChatMessage.class))).thenReturn(chatMessage);

        ChatMessage result = chatService.saveChatMessage(user, chatRoom1, message);

        assertEquals(chatMessage, result);
    }

    @Test
    public void testCreateUnreadCountInfo() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage("Hello");
        chatMessage.setSendTime(LocalDateTime.now());

        ChatRoomUnreadCountInfoDto result = chatService.createUnreadCountInfo(chatRoom1, chatMessage);

        assertEquals(chatRoom1.getChatRoomId(), result.getChatRoomId());
        assertEquals(chatMessage.getMessage(), result.getLatestMessage());
        assertEquals(chatMessage.getSendTime(), result.getLatestMessageTime());
    }

    @Test
    public void testCalculateUnreadMessageCountOnline() {
        userChatRoom2 = UserChatRoom.builder()
                .user(tutor)
                .chatRoom(chatRoom1)
                .isOnline(true)
                .build();

        int unreadCount = chatService.calculateUnreadMessageCount(userChatRoom2, chatRoom1, user.getUserId(),
                tutor.getUserId());

        assertEquals(0, unreadCount);
    }

    @Test
    public void testCalculateUnreadMessageCountSenderAndReceiverIsSame() {
        int unreadCount = chatService.calculateUnreadMessageCount(userChatRoom1, chatRoom1, user.getUserId(),
                user.getUserId());

        assertEquals(0, unreadCount);
    }

    @Test
    public void testHandleUserChatRoomWhenDeleted() {
        User sender = user;
        User receiver = tutor;

        UserChatRoom userChatRoom = UserChatRoom.builder()
                .user(tutor)
                .chatRoom(chatRoom1)
                .isOnline(false)
                .build();
        userChatRoom.setDeletedAt(LocalDateTime.now());

        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomId(1L)
                .initiatedBy(sender)
                .initiatedTo(receiver)
                .userChatRooms(new ArrayList<>(List.of(userChatRoom)))
                .build();

        ChatRoomUnreadCountInfoDto unreadCountInfo = ChatRoomUnreadCountInfoDto.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .latestMessage("Hello")
                .latestMessageTime(LocalDateTime.now())
                .build();

        when(chatMessageService.findMessagesUserNotRead(chatRoom.getChatRoomId(), receiver.getUserId()))
                .thenReturn(Collections.emptyList());

        chatService.handleUserChatRoom(userChatRoom, chatRoom, sender.getUserId(), unreadCountInfo);

        assertNull(userChatRoom.getDeletedAt());
        assertEquals(0, unreadCountInfo.getUnreadMessageCount());
        verify(messageSendingService, times(1)).sendUnreadCountInfo(receiver.getUserId(), unreadCountInfo);
    }

    @Test
    public void testCalculateUnreadMessageCountOffline() {
        userChatRoom2 = UserChatRoom.builder()
                .user(tutor)
                .chatRoom(chatRoom1)
                .isOnline(false)
                .build();

        when(chatMessageService.findMessagesUserNotRead(chatRoom1.getChatRoomId(), tutor.getUserId()))
                .thenReturn(Arrays.asList(new ChatMessage(), new ChatMessage()));

        int unreadCount = chatService.calculateUnreadMessageCount(userChatRoom2, chatRoom1, user.getUserId(),
                tutor.getUserId());

        assertEquals(2, unreadCount);
    }

    @Test
    void sendMessageProcess() {
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        when(chatRoomService.findChatRoomById(chatRoom1.getChatRoomId())).thenReturn(chatRoom1);
        when(chatMessageService.saveMessage(any(ChatMessage.class))).thenReturn(chatMessage1);

        chatService.sendMessageProcess(user.getEmail(), chatRoom1.getChatRoomId(), chatMessage1.getMessage());

        verify(chatMessageService, times(1)).saveMessage(any(ChatMessage.class));
    }

    @Test
    void markMessageAsReadAndSendReceipt() {
        // given
        String email = user.getEmail();
        String messageId = "1";

        chatMessage1 = ChatMessage.builder()
                .id("1")
                .senderId(tutor.getUserId())
                .chatRoomId(chatRoom1.getChatRoomId())
                .message("Hello")
                .sendTime(LocalDateTime.of(2024, 1, 1, 0, 0))
                .isRead(false)
                .build();

        ReadReceiptList readReceiptList = ReadReceiptList.builder()
                .readReceipts(Collections.singletonList(new ReadReceipt(messageId, user.getUserId())))
                .build();

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(chatMessageService.findChatMessageById(messageId)).thenReturn(chatMessage1);
        when(chatRoomService.findChatRoomById(chatMessage1.getChatRoomId())).thenReturn(chatRoom1);
        doNothing().when(chatMessageService).markAsReadAndSave(chatMessage1);
        doNothing().when(messageSendingService).sendReadReceipt(chatRoom1.getChatRoomId(), readReceiptList);

        // when
        chatService.markMessageAsReadAndSendReceipt(email, messageId);

        // then
        verify(chatMessageService, times(1)).markAsReadAndSave(chatMessage1);
        verify(messageSendingService, times(1)).sendReadReceipt(chatRoom1.getChatRoomId(), readReceiptList);

    }
}