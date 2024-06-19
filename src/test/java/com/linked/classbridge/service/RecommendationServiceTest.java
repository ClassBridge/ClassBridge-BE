package com.linked.classbridge.service;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.CategoryType;
import com.linked.classbridge.type.Gender;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

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
        oneDayClass.setClassName("Test Class");
        oneDayClass.setTotalStarRate(4.0);
        oneDayClass.setTotalReviews(10);
        oneDayClass.setTotalWish(50);
        oneDayClass.setCategory(category);
        oneDayClass.setReviewList(new ArrayList<>());

        Lesson lesson = new Lesson();
        lesson.setOneDayClass(oneDayClass);
        lesson.setReviewList(new ArrayList<>());

        oneDayClass.setLessonList(Arrays.asList(lesson));

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
        given(oneDayClassRepository.findAll()).willReturn(Arrays.asList(oneDayClass));

        List<OneDayClass> result = recommendationService.recommendClassesForUser("test@test.com");

        assertEquals(1, result.size());
        assertEquals(oneDayClass, result.get(0));
    }
}
