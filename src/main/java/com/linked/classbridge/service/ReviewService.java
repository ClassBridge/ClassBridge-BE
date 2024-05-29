package com.linked.classbridge.service;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.Review;
import com.linked.classbridge.domain.ReviewImage;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.review.RegisterReviewDto;
import com.linked.classbridge.dto.review.RegisterReviewDto.Request;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.ReviewImageRepository;
import com.linked.classbridge.repository.ReviewRepository;
import com.linked.classbridge.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final ReviewImageRepository reviewImageRepository;

    private final LessonService lessonService;

    private final S3Service s3Service;

    /**
     * 리뷰 등록
     *
     * @param request 리뷰 등록 요청
     * @return 리뷰 등록 응답
     */
    @Transactional
    public RegisterReviewDto.Response registerReview(User user, RegisterReviewDto.Request request) {
        System.out.println("request = " + request);
        validateReview(request);

        Lesson lesson = lessonService.findLessonById(request.lessonId());
        OneDayClass oneDayClass = lesson.getOneDayClass();

        // 클래스 ID가 일치하는지 확인
        if (ObjectUtils.notEqual(request.classId(), oneDayClass.getOneDayClassId())) {
            throw new RestApiException(ErrorCode.INVALID_ONE_DAY_CLASS_ID);
        }

        // 리뷰를 이미 작성했는지 확인 (하나의 레슨에 대해 유저는 하나의 리뷰만 작성 가능)
        reviewRepository.findByLessonAndUser(lesson, user).ifPresent(review -> {
            throw new RestApiException(ErrorCode.REVIEW_ALREADY_EXISTS);
        });

        // 리뷰 등록
        Review savedReview =
                reviewRepository.save(Request.toEntity(user, lesson, oneDayClass, request));

        // 리뷰 이미지 등록
        uploadAndSaveReviewImage(savedReview, request.image1(), request.image2(), request.image3());

        return RegisterReviewDto.Response.fromEntity(savedReview);
    }

    public void validateReview(Request request) {
        if (request.rating() < 0 || request.rating() > 5) {
            throw new RestApiException(ErrorCode.INVALID_REVIEW_RATING);
        }
        if (request.contents().length() < 10 || request.contents().length() > 200) {
            throw new RestApiException(ErrorCode.INVALID_REVIEW_CONTENTS);
        }
    }

    private void uploadAndSaveReviewImage(Review savedReview, MultipartFile... images) {
        // 리뷰 이미지 등록
        int sequence = 1;
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                // 이미지 S3 저장
                String url = s3Service.uploadReviewImage(image);
                // 리뷰 이미지 저장
                reviewImageRepository.save(ReviewImage.builder()
                        .review(savedReview)
                        .url(url)
                        .sequence(sequence++)
                        .build());

            }
        }
    }

}
