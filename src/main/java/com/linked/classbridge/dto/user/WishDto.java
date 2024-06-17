package com.linked.classbridge.dto.user;

import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.type.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WishDto {
    private Long wishId;
    private Long classId;
    private String className;
    private String address;
    private int duration;
    private int price;
    private int personal;
    private CategoryType categoryType;
    private double totalStarRate;
    private long totalReviews;
    private int totalWish;
    private String classImageUrl;

    public WishDto(OneDayClass oneDayClass) {
        this.classId = oneDayClass.getClassId();
        this.className = oneDayClass.getClassName();
        this.address = oneDayClass.getAddress1() + " " + oneDayClass.getAddress2() + " " + oneDayClass.getAddress3();
        this.duration = oneDayClass.getDuration();
        this.price = oneDayClass.getPrice();
        this.personal = oneDayClass.getPersonal();
        this.categoryType = oneDayClass.getCategory().getName();
        this.totalStarRate = oneDayClass.getTotalStarRate();
        this.totalReviews = oneDayClass.getTotalReviews();
        this.totalWish = oneDayClass.getTotalWish();
    }

    public record Request(Long classId) {}

}
