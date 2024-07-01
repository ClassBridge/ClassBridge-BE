package com.linked.classbridge.controller;

import static com.linked.classbridge.type.ErrorCode.BAD_REQUEST;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.chat.ChatMessageDto;
import com.linked.classbridge.dto.chat.ChatRoomUnreadCountInfoDto;
import com.linked.classbridge.dto.chat.CreateChatRoom;
import com.linked.classbridge.dto.chat.CreateChatRoom.Request;
import com.linked.classbridge.dto.chat.GetChatRoomsResponse;
import com.linked.classbridge.dto.chat.JoinChatRoom;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.service.chat.ChatService;
import com.linked.classbridge.type.ErrorCode;
import com.linked.classbridge.type.ResponseMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatRoomController.class)
@TestPropertySource(properties = "spring.config.location=classpath:application-test.yml")
class ChatRoomControllerTest {

    @MockBean
    private ChatService chatService;

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;

    private User mockTutor;

    private CreateChatRoom.Request createChatRoomRequest;

    @BeforeEach
    void setUp() {

        mockTutor = User.builder()
                .userId(1L)
                .email("tutor@mail.com")
                .nickname("tutor")
                .profileImageUrl("profileImageUrl")
                .build();

        mockUser = User.builder()
                .userId(2L)
                .email("user@mail.com")
                .nickname("user")
                .profileImageUrl("profileImageUrl")
                .build();

        createChatRoomRequest = new Request(1L);

        given(userService.getCurrentUser()).willReturn(mockUser);
    }

    @Test
    @WithMockUser
    @DisplayName("채팅방 생성 성공")
    void createChatRoom_success() throws Exception {
        // given
        CreateChatRoom.Response response = new CreateChatRoom.Response(1L, "/chatRooms/1");

        given(chatService.createChatRoomProcess(mockUser, createChatRoomRequest.userId())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/chatRooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(createChatRoomRequest))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ResponseMessage.CHAT_ROOM_CREATE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.chatRoomId").value(1L))
                .andExpect(jsonPath("$.data.chatRoomUrl").value("/chatRooms/1"))

        ;
    }

    @Test
    @WithMockUser
    @DisplayName("채팅방 생성 실패 - 나의 클래스일 경우")
    void createChatRoom_fail_request_to_my_class() throws Exception {
        // given
        given(chatService.createChatRoomProcess(mockUser, createChatRoomRequest.userId()))
                .willThrow(new RestApiException(BAD_REQUEST));

        // when & then
        mockMvc.perform(post("/api/chatRooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(createChatRoomRequest))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("채팅방 참여 성공")
    void joinChatRoom_success() throws Exception {
        // given
        ChatMessageDto chatMessageDto = new ChatMessageDto("messageId", mockUser.getUserId(), "message", false, null);
        JoinChatRoom.Response response = new JoinChatRoom.Response(1L,
                mockUser.getUserId(), mockUser.getUserId(), mockTutor.getUserId(),
                null, null, List.of(chatMessageDto));

        given(chatService.enterChatRoomProcess(mockUser, 1L))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/chatRooms/1/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ResponseMessage.CHAT_ROOM_JOIN_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.chatRoomId").value(1L))
                .andExpect(jsonPath("$.data.senderId").value(mockUser.getUserId()))
                .andExpect(jsonPath("$.data.initiatedBy").value(mockUser.getUserId()))
                .andExpect(jsonPath("$.data.messages").isArray())
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("채팅방 참여 실패 - 채팅방이 없는 경우")
    void joinChatRoom_fail_chat_room_not_exist() throws Exception {
        // given
        given(chatService.enterChatRoomProcess(mockUser, 1L))
                .willThrow(new RestApiException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/chatRooms/1/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.CHAT_ROOM_NOT_FOUND.getDescription()))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("채팅방 목록 조회 성공")
    void getChatRooms_success() throws Exception {
        // given
        User user = User.builder()
                .userId(1L)
                .email("user@mail.com")
                .nickname("user")
                .profileImageUrl("profileImageUrl")
                .build();

        User partner = User.builder()
                .userId(2L)
                .email("user2@mail.com")
                .nickname("user2")
                .profileImageUrl("profileImageUrl2")
                .build();
        ChatRoom chatRoom1 = ChatRoom.builder()
                .chatRoomId(1L)
                .initiatedBy(user)
                .initiatedTo(partner)
                .build();

        ChatRoomUnreadCountInfoDto chatRoomUnreadCountInfoDto = ChatRoomUnreadCountInfoDto.builder()
                .chatRoomId(1L)
                .unreadMessageCount(0)
                .latestMessage("message")
                .latestMessageTime(null)
                .build();

        GetChatRoomsResponse getChatRoomsResponse = GetChatRoomsResponse.builder()
                .userId(user.getUserId())
                .build();
        getChatRoomsResponse.addChatRoomInfo(chatRoom1, partner, chatRoomUnreadCountInfoDto);
        given(userService.getCurrentUser()).willReturn(user);
        given(chatService.getChatRoomListProcess(user)).willReturn(getChatRoomsResponse);

        // when & then
        mockMvc.perform(get("/api/chatRooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.chatRooms[0].chatRoomId").value(chatRoom1.getChatRoomId()))
                .andExpect(jsonPath("$.data.chatRooms[0].chatPartnerId").value(partner.getUserId()))
                .andExpect(jsonPath("$.data.chatRooms[0].chatPartnerNickname").value(partner.getNickname()))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("채팅방 닫기 성공")
    void closeChatRoom_success() throws Exception {
        // given
        doNothing().when(chatService).closeChatRoomProcess(mockUser, 1L);

        // when & then
        mockMvc.perform(post("/api/chatRooms/1/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ResponseMessage.CHAT_ROOM_CLOSE_SUCCESS.getMessage())
                );
    }

    @Test
    @WithMockUser
    @DisplayName("채팅방 퇴장 성공")
    void leaveChatRoom_success() throws Exception {
        // given
        doNothing().when(chatService).leaveChatRoomProcess(mockUser, 1L);

        // when & then
        mockMvc.perform(post("/api/chatRooms/1/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ResponseMessage.CHAT_ROOM_LEAVE_SUCCESS.getMessage())
                );
    }

    @Test
    @WithMockUser
    @DisplayName("채팅방 퇴장 실패 - 채팅방 멤버 아닌 경우")
    void leaveChatRoom_fail_not_chat_room_member() throws Exception {
        // given
        doNothing().when(chatService).leaveChatRoomProcess(mockUser, 1L);
        doThrow(new RestApiException(ErrorCode.USER_NOT_IN_CHAT_ROOM))
                .when(chatService).leaveChatRoomProcess(mockUser, 1L);

        // when & then
        mockMvc.perform(post("/api/chatRooms/1/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_IN_CHAT_ROOM.getDescription())
                );
    }

}