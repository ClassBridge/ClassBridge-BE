package com.linked.classbridge.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.oneDayClass.OneDayClassProjection;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.service.RecommendationService;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.Gender;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
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
    private UserRepository userRepository;

    @MockBean
    private OneDayClassRepository oneDayClassRepository;

    @MockBean
    private RecommendationService recommendationService;

    @Test
    @WithMockUser(username = "USER")
    @DisplayName("사용자에게 클래스 추천 성공")
    public void recommendClassesForUser_success() throws Exception {

        // given
        String userEmail = "test@test.com";
        User user = new User();
        user.setEmail(userEmail);
        user.setBirthDate("2001-05-06");
        user.setGender(Gender.MALE);
        user.setInterests(Arrays.asList(new Category()));

        OneDayClass oneDayClass = new OneDayClass();
        oneDayClass.setClassId(1L);
        oneDayClass.setAverageAge(20.0);
        oneDayClass.setMaleCount(10L);
        oneDayClass.setFemaleCount(5L);
        oneDayClass.setCategory(new Category());

        OneDayClassProjection oneDayClassProjection = new OneDayClassProjection() {
            @Override
            public Long getClassId() {
                return 1L;
            }

            @Override
            public Double getAverageAge() {
                return 20.0;
            }

            @Override
            public Long getMaleCount() {
                return 10L;
            }

            @Override
            public Long getFemaleCount() {
                return 5L;
            }

            @Override
            public Category getCategory() {
                return new Category();
            }
        };

        given(userService.getCurrentUserEmail()).willReturn(userEmail);
        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(user));
        given(oneDayClassRepository.findAllWithSelectedColumns()).willReturn(Arrays.asList(oneDayClassProjection));
        given(oneDayClassRepository.findAllByClassIdIn(Arrays.asList(1L), PageRequest.of(0, 5))).willReturn(new PageImpl<>(Arrays.asList(oneDayClass)));

        // when & then
        mockMvc.perform(get("/api/class/recommend/user-only")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "USER")
    @DisplayName("기본 추천 클래스 조회 성공")
    public void getTopClasses_success() throws Exception {

        // given
        OneDayClass oneDayClass = new OneDayClass();
        oneDayClass.setClassId(1L);

        given(oneDayClassRepository.getTopClassesId(PageRequest.of(0, 5))).willReturn(Arrays.asList(1L, 2L, 3L));
        given(oneDayClassRepository.findAllByClassIdIn(Arrays.asList(1L, 2L, 3L), PageRequest.of(0, 5))).willReturn(new PageImpl<>(Arrays.asList(oneDayClass)));

        // when & then
        mockMvc.perform(get("/api/class/recommend/basic")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }
}