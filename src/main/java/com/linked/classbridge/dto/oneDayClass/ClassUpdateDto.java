package com.linked.classbridge.dto.oneDayClass;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.ClassTag;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.type.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

public class ClassUpdateDto {
    @Builder
    public record ClassRequest(
            @Schema(description = "클래스 이름", example = "헬스 클럽", minLength = 2, maxLength = 20)
            @NotBlank(message = "클래스 이름을 입력해 주세요.")
            @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하로 입력해 주세요.")
            String className,

            @Schema(description = "address1 이름", example = "서울 특별시", minLength = 2, maxLength = 20)
            @NotBlank(message = "시/도 명을 입력해 주세요.")
            @Size(min = 2, max = 20, message = "시/도 명을 입력해 주세요.")
            String address1,

            @Schema(description = "address2 이름", example = "강남구", minLength = 2, maxLength = 20)
            @NotBlank(message = "시/군/구 명을 입력해 주세요.")
            @Size(min = 2, max = 20, message = "시/도 명을 입력해 주세요.")
            String address2,

            @Schema(description = "address3 이름", example = "강남대로 408번길 3층", minLength = 2, maxLength = 20)
            @NotBlank(message = "나머지 주소를 입력해 주세요.")
            @Size(min = 2, max = 50, message = "나머지 주소를 입력해 주세요.")
            String address3,

            @Schema(description = "소요 시간", example = "20", minimum = "0")
            @NotNull(message = "소요 시간(분)을 입력해 주세요.")
            Integer timeTaken,

            @Schema(description = "가격", example = "50000", minimum = "0")
            @NotNull(message = "가격을 입력해 주세요.")
            Integer price,

            @Schema(description = "주차장 정보", example = "저희 건물 앞 주차장을 이용하시면 됩니다.")
            String parkingInformation,

            @Schema(description = "클래스 소개", example = "저희 클래스는 1대1 운동으로, 참여자의 상태에 맞춰 클래스를 진행합니다.", minLength = 2)
            @NotBlank(message = "클래스 소개를 입력해 주세요.")
            @Size(min = 2, message = "클래스 소개는 두 글자 이상 입력해 주세요.")
            String introduction,

            @Schema(description = "클래스 시작일", example = "2024-05-29")
            @NotNull
            LocalDate startDate,

            @Schema(description = "클래스 종료일", example = "2024-06-30")
            @NotNull
            LocalDate endDate,

            @Schema(description = "카테고리", example = "FITNESS")
            @NotNull
            CategoryType categoryType,

            @Schema(description = "tag", example = "tag = {{ name=태그 이름, sequence=순서 }, { }, ...}")
            List<ClassTag> tagList

    ) {
        public static OneDayClass toEntity(ClassUpdateDto.ClassRequest request) {
            return OneDayClass.builder()
                    .className(request.className())
                    .address1(request.address1)
                    .address2(request.address2)
                    .address3(request.address3)
                    .timeTaken(request.timeTaken)
                    .price(request.price)
                    .parkingInformation(request.parkingInformation)
                    .introduction(request.introduction)
                    .startDate(request.startDate)
                    .endDate(request.endDate)
                    .build();
        }
    }

    public record ClassResponse(
            Long classId,

            String className,  // 클래스명

            String address1,    // 시도 명
            String address2,    // 시군구 명
            String address3,    // 나머지 상세 주소

            double latitude,    // 위도
            double longitude,   // 경도

            int timeTaken,     // 소요 시간

            int price,         // 가격

            double totalStarRate, // 총 별점 수
            Integer totalReviews,  // 총 리뷰 수

            String parkingInformation,  // 주차장 정보
            String introduction,        // 클래스 소개

            LocalDate startDate,   // 시작일
            LocalDate endDate,      // 종료일
            Category category,
            Long userId,

            List<ClassTagDto> tagList
    ) {
        public static ClassUpdateDto.ClassResponse fromEntity(OneDayClass oneDayClass) {
            return new ClassUpdateDto.ClassResponse(
                    oneDayClass.getClassId(),
                    oneDayClass.getClassName(),
                    oneDayClass.getAddress1(),
                    oneDayClass.getAddress2(),
                    oneDayClass.getAddress3(),
                    oneDayClass.getLatitude(),
                    oneDayClass.getLongitude(),
                    oneDayClass.getTimeTaken(),
                    oneDayClass.getPrice(),
                    oneDayClass.getTotalStarRate(),
                    oneDayClass.getTotalReviews(),
                    oneDayClass.getParkingInformation(),
                    oneDayClass.getIntroduction(),
                    oneDayClass.getStartDate(),
                    oneDayClass.getEndDate(),
                    oneDayClass.getCategory(),
                    oneDayClass.getTutor().getUserId(),
                    oneDayClass.getTagList().stream().map(ClassTagDto::new).toList());
        }
    }
}
