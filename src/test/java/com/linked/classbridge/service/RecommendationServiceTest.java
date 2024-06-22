package com.linked.classbridge.service;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.oneDayClass.ClassDto;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.CategoryType;
import com.linked.classbridge.type.Gender;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    @InjectMocks
    private RecommendationService recommendationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OneDayClassRepository oneDayClassRepository;

    @Test
    public void recommendClassesForUserTest() throws ExecutionException, InterruptedException {

        Category category = new Category();
        category.setName(CategoryType.COOKING);

        User user = new User();
        user.setEmail("test@test.com");
        user.setGender(Gender.MALE);
        user.setBirthDate("2001-05-06");
        user.setInterests(Collections.singletonList(category));

        OneDayClass oneDayClass = new OneDayClass();
        oneDayClass.setClassId(1L);
        oneDayClass.setClassName("Test Class");
        oneDayClass.setAverageAge(20.0);
        oneDayClass.setMaleCount(10L);
        oneDayClass.setFemaleCount(5L);
        oneDayClass.setCategory(category);
        oneDayClass.setTotalStarRate(4.0);
        oneDayClass.setTotalReviews(10);
        oneDayClass.setTotalWish(5);

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
        given(oneDayClassRepository.findAllIds()).willReturn(Arrays.asList(1L));
        given(oneDayClassRepository.findById(1L)).willReturn(Optional.of(oneDayClass));
        given(oneDayClassRepository.findAllByClassIdIn(Arrays.asList(1L), PageRequest.of(0, 5)))
                .willReturn(new PageImpl<>(Arrays.asList(oneDayClass)));

        List<ClassDto> result = recommendationService.recommendClassesForUser("test@test.com");

        assertEquals(1, result.size());
        assertEquals(oneDayClass.getClassId(), result.get(0).getClassId());
    }
}
