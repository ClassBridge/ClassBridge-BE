package com.linked.classbridge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.service.RecommendationService;
import com.linked.classbridge.service.UserService;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecommendationController.class)
@AutoConfigureMockMvc
public class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RecommendationService recommendationService;

    @MockBean
    private OneDayClassRepository oneDayClassRepository;

    @Test
    @WithMockUser(username = "USER")
    @DisplayName("비동기 추천 클래스 조회 성공")
    public void recommendClassesAsync_success() throws Exception {

        given(userService.getCurrentUserEmail()).willReturn("test@test.com");
        given(recommendationService.recommendClassesForUser("test@test.com")).willReturn(Arrays.asList(new OneDayClass()));

        mockMvc.perform(get("/api/class/recommend/user-only")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "USER")
    @DisplayName("기본 추천 클래스 조회 성공")
    public void recommendClassesBasic_success() throws Exception {

        given(oneDayClassRepository.findTopClassesByRatingAndWish(any(PageRequest.class))).willReturn(Arrays.asList(new OneDayClass()));

        mockMvc.perform(get("/api/class/recommend/basic")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }
}