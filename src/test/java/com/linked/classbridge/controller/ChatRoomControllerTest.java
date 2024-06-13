package com.linked.classbridge.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.chat.ChatMessageDto;
import com.linked.classbridge.dto.chat.ChatRoomDto;
import com.linked.classbridge.dto.chat.CreateChatRoom;
import com.linked.classbridge.dto.chat.CreateChatRoom.Request;
import com.linked.classbridge.dto.chat.JoinChatRoom;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.ChatRoomService;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.ErrorCode;
import com.linked.classbridge.type.ResponseMessage;
import java.util.List;
import java.util.Optional;
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
    private ChatRoomService chatRoomService;

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
                .build();

        mockUser = User.builder()
                .userId(2L)
                .email("user@mail.com")
                .build();

        createChatRoomRequest = new Request(1L);

        given(userService.findByEmail(userService.getCurrentUserEmail()))
                .willReturn(Optional.of(mockUser));
    }

    @Test
    @WithMockUser
    @DisplayName("채팅방 생성 성공")
    void createChatRoom_success() throws Exception {
        // given
        CreateChatRoom.Response response = new CreateChatRoom.Response(1L, "/chatRooms/1");

        given(chatRoomService.createOrGetChatRoom(mockUser, createChatRoomRequest.classId()))
                .willReturn(response);

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
        given(chatRoomService.createOrGetChatRoom(mockUser, createChatRoomRequest.classId()))
                .willThrow(new RestApiException(ErrorCode.BAD_REQUEST));

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

        given(chatRoomService.joinChatRoomAndGetMessages(mockUser, 1L))
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
        given(chatRoomService.joinChatRoomAndGetMessages(mockUser, 1L))
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
        ChatRoomDto chatRoomDto = new ChatRoomDto();
        ChatRoomDto.inquiredChatRooms inquiredChatRooms = new ChatRoomDto.inquiredChatRooms(1L, 1L, 2L, "tutor", "url");
        ChatRoomDto.receivedInquiryChatRooms receivedInquiryChatRooms = new ChatRoomDto.receivedInquiryChatRooms(2L, 3L,
                "user", "user", 2L);
        chatRoomDto.getInquiredChatRoomsChatRooms().add(inquiredChatRooms);
        chatRoomDto.getReceivedInquiryChatRoomsChatRooms().add(receivedInquiryChatRooms);
        given(chatRoomService.getChatRooms(mockUser))
                .willReturn(chatRoomDto);

        // when & then
        mockMvc.perform(get("/api/chatRooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inquiredChatRoomsChatRooms[0].chatRoomId").value(1L))
                .andExpect(jsonPath("$.data.receivedInquiryChatRoomsChatRooms[0].chatRoomId").value(2L))
        ;
    }

}