package com.linked.classbridge.dto.oneDayClass;

import com.linked.classbridge.domain.ClassFAQ;
import com.linked.classbridge.domain.ClassImage;
import com.linked.classbridge.domain.ClassTag;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.repository.ClassImageRepository;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.repository.WishRepository;
import com.linked.classbridge.type.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
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
public class ClassDto {
    private Long classId;
    private String className;
    private String tutorName;
    private String address;
    private String address1;
    private String address2;
    private String address3;
    private int duration;
    private int price;
    private int personal;
    private boolean hasParking;
    private String introduction;
    private LocalDate startDate;
    private LocalDate endDate;
    private CategoryType category;
    private double totalStarRate;
    private long totalReviews;
    private int totalWish;
    private Double starRate;
    private boolean wish;
    private String classImageUrl;

    public ClassDto(OneDayClass oneDayClass) {
        this.classId = oneDayClass.getClassId();
        this.className = oneDayClass.getClassName();
        this.address = oneDayClass.getAddress1() + " " + oneDayClass.getAddress2() + " " + oneDayClass.getAddress3();
        this.address1 = oneDayClass.getAddress1();
        this.address2 = oneDayClass.getAddress2();
        this.address3 = oneDayClass.getAddress3();
        this.duration = oneDayClass.getDuration();
        this.price = oneDayClass.getPrice();
        this.personal = oneDayClass.getPersonal();
        this.hasParking = oneDayClass.isHasParking();
        this.introduction = oneDayClass.getIntroduction();
        this.startDate = oneDayClass.getStartDate();
        this.endDate = oneDayClass.getEndDate();
        this.category = oneDayClass.getCategory().getName();
        this.totalStarRate = oneDayClass.getTotalStarRate();
        this.totalReviews = oneDayClass.getTotalReviews();
        this.totalWish = oneDayClass.getTotalWish();

        if(oneDayClass.getTotalReviews() == 0){
            this.starRate = 0.0;
        } else {
            DecimalFormat df = new DecimalFormat("#.#");
            this.starRate = Double.valueOf(df.format(oneDayClass.getTotalStarRate() / oneDayClass.getTotalReviews()));
        }
    }

    public void setTutorName(OneDayClassRepository oneDayClassRepository) {

        String tutorName = oneDayClassRepository.findTutorNameByClassId(this.classId);
        this.tutorName = tutorName;
    }

    public void setIsWish(User user, WishRepository wishRepository) {

        if(user == null || wishRepository == null){
            this.wish = false;
            return;
        }

        if(wishRepository.existsByUserUserIdAndOneDayClassClassId(user.getUserId(), this.classId)){
            this.wish = true;
        } else {
            this.wish = false;
        }
    }

    public void setClassImage(ClassImageRepository classImageRepository) {

        ClassImage classImage = classImageRepository.findFirstByOneDayClassClassIdAndSequence(this.classId, 1)
                .orElse(null);
        this.classImageUrl = classImage != null ? classImage.getUrl() : null;
    }

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
            Integer duration,

            @Schema(description = "가격", example = "50000", minimum = "0")
            @NotNull(message = "가격을 입력해 주세요.")
            Integer price,

            @Schema(description = "최대 인원", example = "6", minimum = "0")
            @NotNull(message = "최대 인원을 입력해 주세요.")
            Integer personal,

            @Schema(description = "주차장 정보", example = "true")
            boolean hasParking,

            @Schema(description = "클래스 소개", example = "저희 클래스는 1대1 운동으로, 참여자의 상태에 맞춰 클래스를 진행합니다.", minLength = 2)
            @NotBlank(message = "클래스 소개를 입력해 주세요.")
            @Size(min = 20, max=500, message = "클래스 소개는 20 글자 이상 500 글자 이하로 입력해 주세요.")
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

            @Schema(description = "반복 요일 강의 시간", example = "{{dayOfWeek=MONDAY, times={14:00:00, 18:00:00}, { }, ...}")
            List<RepeatClassDto> lesson,

            @Schema(description = "faq", example = "faq = {{ title=제목, content = 내용}, { }, ...}")
            List<ClassFAQ> faqList,

            @Schema(description = "tag", example = "tag = {{ name=태그 이름}, { }, ...}")
            List<ClassTag> tagList

    ) {
        public static OneDayClass toEntity(ClassRequest request) {
            return OneDayClass.builder()
                    .className(request.className())
                    .address1(request.address1)
                    .address2(request.address2)
                    .address3(request.address3)
                    .duration(request.duration)
                    .price(request.price)
                    .personal(request.personal)
                    .hasParking(request.hasParking)
                    .introduction(request.introduction)
                    .totalStarRate(0.0)
                    .totalReviews(0)
                    .totalWish(0)
                    .startDate(request.startDate)
                    .endDate(request.endDate)
                    .build();
        }
    }

    public record ClassResponseByTutor(
            Long classId,

            String className,  // 클래스명

            String address1,    // 시도 명
            String address2,    // 시군구 명
            String address3,    // 나머지 상세 주소

            double latitude,    // 위도
            double longitude,   // 경도

            int duration,     // 소요 시간

            int price,         // 가격
            int personal,

            Double totalStarRate, // 총 별점 수
            Integer totalReviews,  // 총 리뷰 수
            Integer totalWish,     // 총 찜 수

            boolean hasParking,  // 주차장 정보
            String introduction,        // 클래스 소개

            LocalDate startDate,   // 시작일
            LocalDate endDate,      // 종료일
            CategoryType category,
            Long userId,   // 강사 id
            boolean isWish,  // 찜 여부

            List<ClassImageDto> imageList,
            List<LessonDtoDetail> lessonList,
            List<ClassFAQDto> faqList,
            List<ClassTagDto> tagList
    ) {
        public static ClassResponseByTutor fromEntity(OneDayClass oneDayClass) {
            return new ClassResponseByTutor(
                    oneDayClass.getClassId(),
                    oneDayClass.getClassName(),
                    oneDayClass.getAddress1(),
                    oneDayClass.getAddress2(),
                    oneDayClass.getAddress3(),
                    oneDayClass.getLatitude(),
                    oneDayClass.getLongitude(),
                    oneDayClass.getDuration(),
                    oneDayClass.getPrice(),
                    oneDayClass.getPersonal(),
                    oneDayClass.getTotalStarRate(),
                    oneDayClass.getTotalReviews(),
                    oneDayClass.getTotalWish(),
                    oneDayClass.isHasParking(),
                    oneDayClass.getIntroduction(),
                    oneDayClass.getStartDate(),
                    oneDayClass.getEndDate(),
                    oneDayClass.getCategory().getName(),
                    oneDayClass.getTutor().getUserId(),
                    false,
                    oneDayClass.getImageList().stream().map(ClassImageDto::new).toList(),
                    oneDayClass.getLessonList().stream().map(LessonDtoDetail::new).toList(),
                    oneDayClass.getFaqList().stream().map(ClassFAQDto::new).toList(),
                    oneDayClass.getTagList().stream().map(ClassTagDto::new).toList());
        }
    }

    public record ClassResponseByUser(
            Long classId,

            String className,  // 클래스명

            String address1,    // 시,도
            String address2,    // 시,군,구
            String address3,    // 상세 주소

            double latitude,    // 위도
            double longitude,   // 경도

            int duration,     // 소요 시간

            int price,         // 가격
            int personal,

            Double totalStarRate, // 총 별점 수
            Integer totalReviews,  // 총 리뷰 수
            Integer totalWish,     // 총 찜 수

            boolean hasParking,  // 주차장 정보
            String introduction,        // 클래스 소개

            LocalDate startDate,   // 시작일
            LocalDate endDate,      // 종료일
            CategoryType category,
            Long tutorId,   // 강사 id
            String tutorName,  // 강사 닉네임
            String tutorIntroduction,
            boolean isWish,  // 찜 여부
            boolean isWanted, // 수강 신청 가능 여부
            List<ClassImageDto> imageList,
            List<LessonDto> lessonList,
            List<ClassFAQDto> faqList,
            List<ClassTagDto> tagList
    ) {
        public static ClassResponseByUser fromEntity(OneDayClass oneDayClass, boolean isWish, boolean isWanted) {
            return new ClassResponseByUser(
                    oneDayClass.getClassId(),
                    oneDayClass.getClassName(),
                    oneDayClass.getAddress1(),
                    oneDayClass.getAddress2(),
                    oneDayClass.getAddress3(),
                    oneDayClass.getLatitude(),
                    oneDayClass.getLongitude(),
                    oneDayClass.getDuration(),
                    oneDayClass.getPrice(),
                    oneDayClass.getPersonal(),
                    oneDayClass.getTotalStarRate(),
                    oneDayClass.getTotalReviews(),
                    oneDayClass.getTotalWish(),
                    oneDayClass.isHasParking(),
                    oneDayClass.getIntroduction(),
                    oneDayClass.getStartDate(),
                    oneDayClass.getEndDate(),
                    oneDayClass.getCategory().getName(),
                    oneDayClass.getTutor().getUserId(),
                    oneDayClass.getTutor().getNickname(),
                    oneDayClass.getTutor().getSelfIntroduction(),
                    isWish,
                    isWanted,
                    oneDayClass.getImageList().stream().map(ClassImageDto::new).toList(),
                    oneDayClass.getLessonList().stream().map(LessonDto::new).toList(),
                    oneDayClass.getFaqList().stream().map(ClassFAQDto::new).toList(),
                    oneDayClass.getTagList().stream().map(ClassTagDto::new).toList());
        }
    }
}
