package com.linked.classbridge.dto.oneDayClass;

import com.linked.classbridge.domain.document.OneDayClassDocument;
import com.linked.classbridge.type.CategoryType;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ClassSearchDto {
    private Long classId;
    private String className;   // 클래스명
    private String tutorName;   // 강사 닉네임
    private String address;    // 시도 명
    private double lat;     // 위도
    private double lng;     // 경도
    private int duration;      // 소요 시간
    private int price;          // 가격
    private int personal;   // 수강 최대 인원
    private Double starRate; // 별점
    private int totalWish;  // 총 찜 개수
    private String imageUrl;
    private List<String> tagList;
    private boolean hasParking;  // 주차장
    private LocalDate startDate;    // 시작일
    private LocalDate endDate;      // 종료일
    private CategoryType category;
    private boolean isWish;

    public ClassSearchDto(OneDayClassDocument oneDayClassDocument) {
        this.classId = oneDayClassDocument.getClassId();
        this.className = oneDayClassDocument.getClassName();
        this.tutorName = oneDayClassDocument.getTutorName();
        this.address = oneDayClassDocument.getAddress1() + " " + oneDayClassDocument.getAddress2() + " " + oneDayClassDocument.getAddress3();
        this.lat = oneDayClassDocument.getLocation().getLat();
        this.lng = oneDayClassDocument.getLocation().getLon();
        this.duration = oneDayClassDocument.getDuration();
        this.price = oneDayClassDocument.getPrice();
        this.starRate = oneDayClassDocument.getStarRate();
        this.totalWish = oneDayClassDocument.getTotalWish();
        this.imageUrl = oneDayClassDocument.getImageUrl();
        this.tagList = oneDayClassDocument.getTagList();
        this.endDate = oneDayClassDocument.getEndDate();
        this.category = oneDayClassDocument.getCategory();
    }
}
