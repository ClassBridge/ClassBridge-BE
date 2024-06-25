package com.linked.classbridge.service;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.oneDayClass.ClassDto;
import com.linked.classbridge.dto.oneDayClass.OneDayClassProjection;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.ClassImageRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

    private final UserRepository userRepository;
    private final OneDayClassRepository oneDayClassRepository;
    private final ClassImageRepository classImageRepository;

    public RecommendationService(UserRepository userRepository, OneDayClassRepository oneDayClassRepository,
                                 ClassImageRepository classImageRepository) {

        this.userRepository = userRepository;
        this.oneDayClassRepository = oneDayClassRepository;
        this.classImageRepository = classImageRepository;
    }

    // 사용자에게 맞는 추천 클래스 반환
    public List<ClassDto> recommendClassesForUser(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RestApiException(ErrorCode.USER_NOT_FOUND));

        // 유저의 나이, 성별, 관심 카테고리 정보 가져오기
        int userAge = AgeUtil.calculateAge(user.getBirthDate());
        Gender userGender = user.getGender();
        List<Long> userInterestsId = user.getInterests() != null ?
                user.getInterests().stream().map(Category::getCategoryId).toList() : null;

        if(userAge == 0 || userGender == null || userInterestsId == null) {
            return getTopClasses();
        }

        // 조회 최적화를 위해 인터페이스 기반 Projection 사용
        List<OneDayClassProjection> allClasses = oneDayClassRepository.findAllWithSelectedColumns();

        Map<Long, Double> correlationScores = new HashMap<>();

        for (OneDayClassProjection oneDayClass : allClasses) {

            double classAge = oneDayClass.getAverageAge() != null ? oneDayClass.getAverageAge() : 20.0; // 평균 연령 20세가 기본값
            Long maleCount = oneDayClass.getMaleCount() != null ? oneDayClass.getMaleCount() : 0L;
            Long femaleCount = oneDayClass.getFemaleCount() != null ? oneDayClass.getFemaleCount() : 0L;
            Gender classGender = maleCount > femaleCount ? Gender.MALE : Gender.FEMALE;

            double matchInterest = userInterestsId.stream()
                    .anyMatch(id -> id.equals(oneDayClass.getCategory().getCategoryId())) ? 1 : 0;
            double[] userVector = {userAge, userGender.ordinal(), matchInterest};
            double[] classVector = {classAge, classGender.ordinal(), 1};

            // 두 벡터 간의 피어슨 상관 계수를 계산
            PearsonsCorrelation correlation = new PearsonsCorrelation();
            double correlationScore = correlation.correlation(userVector, classVector);

            correlationScores.put(oneDayClass.getClassId(), correlationScore);
        }

        List<Long> topClassIds = correlationScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Page<OneDayClass> page = oneDayClassRepository.findAllByClassIdIn(topClassIds, PageRequest.of(0, 5));

        return page.getContent().stream().map(oneDayClass -> {
            ClassDto classDto = new ClassDto(oneDayClass);
            classDto.setClassImage(classImageRepository);
            return classDto;
        }).collect(Collectors.toList());
    }

    // 기본 추천 클래스 반환
    public List<ClassDto> getTopClasses() {

        List<Long> topClassIds = oneDayClassRepository.getTopClassesId(PageRequest.of(0, 5));

        return oneDayClassRepository
                .findAllByClassIdIn(topClassIds, PageRequest.of(0, 5))
                .getContent().stream().map(oneDayClass -> {
            ClassDto classDto = new ClassDto(oneDayClass);
            classDto.setClassImage(classImageRepository);
            return classDto;
        }).collect(Collectors.toList());
    }
}
