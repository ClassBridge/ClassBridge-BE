package com.linked.classbridge.service;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.Review;
import com.linked.classbridge.domain.ReviewImage;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.review.DeleteReviewResponse;
import com.linked.classbridge.dto.review.RegisterReviewDto;
import com.linked.classbridge.dto.review.RegisterReviewDto.Request;
import com.linked.classbridge.dto.review.UpdateReviewDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.ReviewImageRepository;
import com.linked.classbridge.repository.ReviewRepository;
import com.linked.classbridge.type.ErrorCode;
import java.util.List;
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
        validateRegisterReview(request);

        Lesson lesson = lessonService.findLessonById(request.lessonId());
        OneDayClass oneDayClass = lesson.getOneDayClass();

        if (ObjectUtils.notEqual(request.classId(), oneDayClass.getOneDayClassId())) {
            throw new RestApiException(ErrorCode.INVALID_ONE_DAY_CLASS_ID);
        }

        validateReviewAlreadyExists(user, lesson);

        Review savedReview =
                reviewRepository.save(Request.toEntity(user, lesson, oneDayClass, request));

        uploadAndSaveReviewImage(savedReview, request.image1(), request.image2(), request.image3());
        oneDayClass.addReview(savedReview); // 평점 등록

        return RegisterReviewDto.Response.fromEntity(savedReview);
    }

    /**
     * 리뷰 수정
     *
     * @param user     사용자
     * @param request  수정할 리뷰 정보
     * @param reviewId 리뷰 ID
     * @return 수정된 리뷰 응답
     */
    @Transactional
    public UpdateReviewDto.Response updateReview(User user, UpdateReviewDto.Request request,
                                                 Long reviewId) {
        validateUpdateReview(request);

        Review review = findReviewById(reviewId);
        OneDayClass oneDayClass = review.getOneDayClass();

        validateReviewOwner(user, review);

        updateReviewImage(review, request.image1(), request.image2(), request.image3());

        Double prevRating = review.getRating();
        Double diffRating = request.rating() - prevRating; // 평점 차이 계산

        review.update(request.contents(), request.rating());

        oneDayClass.updateTotalStarRate(diffRating); // 평점 업데이트

        return UpdateReviewDto.Response.fromEntity(review);
    }

    @Transactional
    public DeleteReviewResponse deleteReview(User user, Long reviewId) {

        Review review = findReviewById(reviewId);

        validateReviewOwner(user, review);

        List<ReviewImage> reviewImages =
                reviewImageRepository.findByReviewOrderBySequenceAsc(review);

        reviewImages.forEach(reviewImage -> s3Service.delete(reviewImage.getUrl()));

        review.getOneDayClass().removeReview(review);

        reviewRepository.delete(review);

        return new DeleteReviewResponse(reviewId);
    }

    private void validateReviewAlreadyExists(User user, Lesson lesson) {
        reviewRepository.findByLessonAndUser(lesson, user).ifPresent(review -> {
            throw new RestApiException(ErrorCode.REVIEW_ALREADY_EXISTS);
        });
    }

    public void validateRegisterReview(RegisterReviewDto.Request request) {
        validateReviewRating(request.rating());
        validateReviewContents(request.contents());
    }

    private void validateReviewRating(Double rating) {
        if (rating < 0 || rating > 5) {
            throw new RestApiException(ErrorCode.INVALID_REVIEW_RATING);
        }
    }

    private void validateReviewContents(String contents) {
        if (contents.length() < 10 || contents.length() > 200) {
            throw new RestApiException(ErrorCode.INVALID_REVIEW_CONTENTS);
        }
    }

    private void uploadAndSaveReviewImage(Review savedReview, MultipartFile... images) {
        // 리뷰 이미지 등록
        int sequence = 1;
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                String url = s3Service.uploadReviewImage(image);
                reviewImageRepository.save(ReviewImage.builder()
                        .review(savedReview)
                        .url(url)
                        .sequence(sequence++)
                        .build());

            }
        }
    }


    public void validateUpdateReview(UpdateReviewDto.Request request) {
        validateReviewRating(request.rating());
        validateReviewContents(request.contents());
    }

    private void validateReviewOwner(User user, Review review) {
        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new RestApiException(ErrorCode.NOT_REVIEW_OWNER);
        }
    }

    public Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RestApiException(ErrorCode.REVIEW_NOT_FOUND));
    }


    private void updateReviewImage(Review review, MultipartFile... images) {
        List<ReviewImage> reviewImages =
                reviewImageRepository.findByReviewOrderBySequenceAsc(review);
        int sequence = 1;
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                if (sequence > reviewImages.size()) {
                    String url = s3Service.uploadReviewImage(image);
                    reviewImageRepository.save(ReviewImage.builder()
                            .review(review)
                            .url(url)
                            .sequence(sequence)
                            .build());
                } else {
                    ReviewImage reviewImage = reviewImages.get(sequence - 1);
                    String prevImageUrl = reviewImage.getUrl();
                    s3Service.delete(prevImageUrl);
                    String url = s3Service.uploadReviewImage(image);
                    reviewImage.updateUrl(url);
                }
            }
            sequence++;
        }
    }
}
