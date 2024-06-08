package com.linked.classbridge.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "UPDATE one_day_class SET deleted_at = NOW() WHERE class_id = ?")
@ToString
public class OneDayClass extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private Long classId;

    @Column(nullable = false)
    private String className;   // 클래스명

    @Column(nullable = false)
    private String address1;    // 시도 명
    @Column(nullable = false)
    private String address2;    // 시군구 명
    @Column(nullable = false)
    private String address3;    // 나머지 상세 주소
    @Column(nullable = false)
    private double latitude;    // 위도
    @Column(nullable = false)
    private double longitude;   // 경도
    @Column(nullable = false)
    private int duration;      // 소요 시간
    @Column(nullable = false)
    private int price;          // 가격
    @Column(nullable = false)
    @Min(0)
    private int personal;   // 수강 최대 인원
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Double totalStarRate; // 총 별점 수
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Integer totalReviews;  // 총 리뷰 수

    private boolean hasParking;  // 주차장
    @Column(nullable = false)
    private String introduction;        // 클래스 소개

    @Column(nullable = false)
    private LocalDate startDate;    // 시작일
    @Column(nullable = false)
    private LocalDate endDate;      // 종료일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User tutor;


    @OneToMany(mappedBy = "oneDayClass", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ClassImage> imageList;

    @OneToMany(mappedBy = "oneDayClass", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Lesson> lessonList;

    @OneToMany(mappedBy = "oneDayClass", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviewList;

    @OneToMany(mappedBy = "oneDayClass", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ClassFAQ> faqList;

    @OneToMany(mappedBy = "oneDayClass", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ClassTag> tagList;

     public void addReview(Review review) {
        this.reviewList.add(review);
        this.totalStarRate += review.getRating();
        this.totalReviews++;
    }

    public void removeReview(Review review) {
        this.reviewList.remove(review);
        this.totalStarRate -= review.getRating();
        this.totalReviews--;
    }

    public void updateTotalStarRate(Double diff) {
        this.totalStarRate += diff;
    }
}
