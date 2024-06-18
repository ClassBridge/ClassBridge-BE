package com.linked.classbridge.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.linked.classbridge.dto.badge.BadgeResponse;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.BadgeService;
import com.linked.classbridge.service.JWTService;
import com.linked.classbridge.type.ErrorCode;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(BadgeController.class)
@AutoConfigureMockMvc
public class BadgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BadgeService badgeService;

    @MockBean
    private JWTService jwtService;

    @Test
    @WithMockUser(username = "USER")
    @DisplayName("뱃지 수동 추가 성공")
    public void uploadBadge_success() throws Exception {

        String badgeName = "testBadge";
        MockMultipartFile badgeImage =
                new MockMultipartFile("badgeImage", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test".getBytes());
        Long categoryId = 1L;
        int threshold = 1;

        doNothing().when(badgeService).uploadBadge(badgeName, badgeImage, categoryId, threshold);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/users/badges/add/{badgeName}", badgeName)
                        .file(badgeImage)
                        .param("categoryId", String.valueOf(categoryId))
                        .param("threshold", String.valueOf(threshold))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "USER")
    @DisplayName("뱃지 추가 실패 - 카테고리 없음")
    public void uploadBadge_failure_no_such_category() throws Exception {

        String badgeName = "testBadge";
        MockMultipartFile badgeImage =
                new MockMultipartFile("badgeImage", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test".getBytes());
        Long categoryId = 999L; // 존재하지 않는 카테고리 ID
        int threshold = 1;

        doThrow(new RestApiException(ErrorCode.CATEGORY_NOT_FOUND)).when(badgeService).uploadBadge(badgeName, badgeImage, categoryId, threshold);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/users/badges/add/{badgeName}", badgeName)
                        .file(badgeImage)
                        .param("categoryId", String.valueOf(categoryId))
                        .param("threshold", String.valueOf(threshold))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "USER")
    @DisplayName("획득한 뱃지 리스트 조회 성공")
    public void getBadges_success() throws Exception {

        String userEmail = "test@test.com";
        BadgeResponse badgeResponse = BadgeResponse.builder()
                .name("testBadge")
                .imageUrl("testUrl")
                .build();

        List<BadgeResponse> badgeList = Collections.singletonList(badgeResponse);

        when(badgeService.getBadges(userEmail)).thenReturn(badgeList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/badges")
                        .header("access", "testToken")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
