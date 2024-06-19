package com.linked.classbridge.service;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.ErrorCode;
import com.linked.classbridge.type.Gender;
import com.linked.classbridge.util.AgeUtil;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

    private final UserRepository userRepository;
    private final OneDayClassRepository oneDayClassRepository;

    public RecommendationService(UserRepository userRepository, OneDayClassRepository oneDayClassRepository) {

        this.userRepository = userRepository;
        this.oneDayClassRepository = oneDayClassRepository;
    }

    public List<OneDayClass> recommendClassesForUser(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RestApiException(ErrorCode.USER_NOT_FOUND));

        // 유저의 나이, 성별, 관심 카테고리 정보 가져오기
        int userAge = AgeUtil.calculateAge(user.getBirthDate());
        Gender userGender = user.getGender();
        List<Category> userInterests = user.getInterests();

        if(userAge == 0 || userGender == null || userInterests == null) {
            return oneDayClassRepository.findTopClassesByRatingAndWish(PageRequest.of(0, 5));
        }

        List<OneDayClass> allClasses = oneDayClassRepository.findAll();

        Map<OneDayClass, Double> correlationScores = new HashMap<>();

        for (OneDayClass oneDayClass : allClasses) {
            double classAge = oneDayClass.getAverageAge() != null ? oneDayClass.getAverageAge() : 20.0; // 평균 연령 20세가 기본값
            Long maleCount = oneDayClass.getMaleCount() != null ? oneDayClass.getMaleCount() : 0L;
            Long femaleCount = oneDayClass.getFemaleCount() != null ? oneDayClass.getFemaleCount() : 0L;
            Gender classGender = maleCount > femaleCount ? Gender.MALE : Gender.FEMALE;

            double[] userVector = {userAge, userGender.ordinal(), userInterests.contains(oneDayClass.getCategory()) ? 1 : 0};
            double[] classVector = {classAge, classGender.ordinal(), 1};

            // 두 벡터 간의 피어슨 상관 계수를 계산
            PearsonsCorrelation correlation = new PearsonsCorrelation();
            double correlationScore = correlation.correlation(userVector, classVector);

            correlationScores.put(oneDayClass, correlationScore);
        }

        return correlationScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
