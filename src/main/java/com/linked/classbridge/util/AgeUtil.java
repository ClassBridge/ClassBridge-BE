package com.linked.classbridge.util;

import java.time.LocalDate;
import java.time.Period;

public class AgeUtil {

    public static int calculateAge(String birthDateString) {

        if(birthDateString == null) {
            return 20; // 기본값 20세 (User 엔티티에는 영향을 주지 않음, 클래스 추천 로직에서만 사용)
        }

        LocalDate birthDate = LocalDate.parse(birthDateString);
        LocalDate now = LocalDate.now();
        int age = Period.between(birthDate, now).getYears();

        if (now.isBefore(birthDate.withYear(now.getYear()))) {
            age--; // 생일이 아직 지나지 않았다면 1 빼기
        }

        return age;
    }
}