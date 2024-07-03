package com.linked.classbridge.domain.document;

import com.linked.classbridge.domain.ClassTag;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.type.CategoryType;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(indexName = "onedayclass")
@Setting(settingPath = "elasticsearch/setting.json")
@Mapping(mappingPath = "elasticsearch/mapping.json")
public class OneDayClassDocument {
    @Id
    private Long classId;
    private String className;   // 클래스명
    private String tutorName;   // 강사 닉네임
    private String address1;    // 시도 명
    private String address2;    // 시군구 명
    private String address3;    // 나머지 상세 주소
    @GeoPointField
    private GeoPoint location;
    private int duration;      // 소요 시간
    private int price;          // 가격
    private int totalReviews;   // 총 댓글 수
    private Double starRate; // 별점
    private int totalWish;  // 총 찜 개수

    private String imageUrl;

    private List<String> tagList;

    @Field(type= FieldType.Date, format = DateFormat.date)
    private LocalDate endDate;      // 종료일
    private CategoryType category;

    public OneDayClassDocument(OneDayClass oneDayClass) {
        this.classId = oneDayClass.getClassId();
        this.className = oneDayClass.getClassName();
        this.tutorName = oneDayClass.getTutor().getNickname();
        this.address1 = oneDayClass.getAddress1();
        this.address2 = oneDayClass.getAddress2();
        this.address3 = oneDayClass.getAddress3();
        location = new GeoPoint(oneDayClass.getLatitude(), oneDayClass.getLongitude());
        duration = oneDayClass.getDuration();
        price = oneDayClass.getPrice();
        totalReviews = oneDayClass.getTotalReviews();
        starRate = oneDayClass.getTotalStarRate() / (oneDayClass.getTotalReviews() == 0 ? 1 : oneDayClass.getTotalReviews());
        totalWish = oneDayClass.getTotalWish();
        endDate = oneDayClass.getEndDate();
        category = oneDayClass.getCategory().getName();
        tagList = oneDayClass.getTagList().stream().map(ClassTag::getName).toList();
        imageUrl = !oneDayClass.getImageList().isEmpty() ?oneDayClass.getImageList().get(0).getUrl() : null;
    }

}
