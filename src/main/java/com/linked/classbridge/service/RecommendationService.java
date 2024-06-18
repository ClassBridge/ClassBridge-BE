package com.linked.classbridge.service;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.repository.ReservationRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.ErrorCode;
import com.linked.classbridge.type.Gender;
import com.linked.classbridge.util.AgeUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

    private final UserRepository userRepository;
    private final OneDayClassRepository oneDayClassRepository;
    private final ReservationRepository reservationRepository;

    public RecommendationService(UserRepository userRepository, OneDayClassRepository oneDayClassRepository,
                                 ReservationRepository reservationRepository) {

        this.userRepository = userRepository;
        this.oneDayClassRepository = oneDayClassRepository;
        this.reservationRepository = reservationRepository;
    }

    public CompletableFuture<List<OneDayClass>> recommendClasses(String userEmail) {

        return CompletableFuture.supplyAsync(() -> {

            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RestApiException(ErrorCode.USER_NOT_FOUND));

            // 유저의 나이, 성별, 관심 카테고리 정보 가져오기
            int userAge = AgeUtil.calculateAge(user.getBirthDate());
            Gender userGender = user.getGender();
            List<Category> userInterests = user.getInterests();

            List<OneDayClass> allClasses = oneDayClassRepository.findAll();

            Map<OneDayClass, Double> correlationScores = new HashMap<>();

            for (OneDayClass oneDayClass : allClasses) {
                List<Lesson> lessons = oneDayClass.getLessonList();
                List<Reservation> reservations = new ArrayList<>();

                for (Lesson lesson : lessons) {
                    reservations.addAll(reservationRepository.findAllByLesson(lesson));
                }

                // 클래스를 듣는 유저의 평균 나이 구하기
                double classAge = reservations.stream()
                        .mapToInt(reservation -> AgeUtil.calculateAge(reservation.getUser().getBirthDate()))
                        .average()
                        .orElse(20); // 기본 평균값은 20

                // 클래스를 듣는 유저들의 전반적인 성별 구하기
                Map<Gender, Long> genderCount = reservations.stream()
                        .collect(Collectors.groupingBy(reservation -> reservation.getUser().getGender(), Collectors.counting()));

                Long maleCount = genderCount.getOrDefault(Gender.MALE, 0L);
                Long femaleCount = genderCount.getOrDefault(Gender.FEMALE, 0L);
                Gender classGender = maleCount > femaleCount ? Gender.MALE : Gender.FEMALE;

                double[] userVector = {userAge, userGender.ordinal(), userInterests.contains(oneDayClass.getCategory()) ? 1 : 0};
                double[] classVector = {classAge, classGender.ordinal(), 1};

                // 두 벡터 간의 피어슨 상관 계수를 계산
                PearsonsCorrelation correlation = new PearsonsCorrelation();
                double correlationScore = correlation.correlation(userVector, classVector);

                // 가중치 계산
                double ageWeight = 1.0 - Math.abs(userAge - classAge) / 100.0;  // 나이 차이가 클수록 가중치 감소
                double genderWeight = (userGender == classGender) ? 1.0 : 0.5;  // 성별이 다르면 가중치 감소

                correlationScore *= ageWeight * genderWeight;

                correlationScores.put(oneDayClass, correlationScore);
            }

            return correlationScores.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        });
    }
}
