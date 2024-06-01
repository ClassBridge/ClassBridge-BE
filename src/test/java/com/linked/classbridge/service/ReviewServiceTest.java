package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.INVALID_ONE_DAY_CLASS_ID;
import static com.linked.classbridge.type.ErrorCode.INVALID_REVIEW_CONTENTS;
import static com.linked.classbridge.type.ErrorCode.INVALID_REVIEW_RATING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.Review;
import com.linked.classbridge.domain.ReviewImage;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.review.DeleteReviewResponse;
import com.linked.classbridge.dto.review.GetReviewResponse;
import com.linked.classbridge.dto.review.RegisterReviewDto;
import com.linked.classbridge.dto.review.RegisterReviewDto.Request;
import com.linked.classbridge.dto.review.UpdateReviewDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.ReviewImageRepository;
import com.linked.classbridge.repository.ReviewRepository;
import com.linked.classbridge.type.ErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @Mock
    private LessonService lessonService;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ReviewService reviewService;

    private User mockUser;
    private Lesson mockLesson;
    private OneDayClass mockOneDayClass;


    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .userId(1L)
                .nickname("user")
                .build();
        mockOneDayClass = OneDayClass.builder()
                .oneDayClassId(1L)
                .className("oneDayClassName")
                .reviewList(new ArrayList<>())
                .totalStarRate(0.0)
                .totalReviews(0)
                .build();
        mockLesson = Lesson.builder()
                .lessonId(1L)
                .oneDayClass(mockOneDayClass)
                .lessonDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("리뷰 조회 성공")
    void getReview_success() {
        // given
        Review review = Review.builder()
                .reviewId(1L)
                .user(mockUser)
                .lesson(mockLesson)
                .oneDayClass(mockOneDayClass)
                .contents("This is a contents.")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .reviewImageList(List.of(
                        ReviewImage.builder().url("url1").build(),
                        ReviewImage.builder().url("url2").build(),
                        ReviewImage.builder().url("url3").build()
                ))
                .build();

        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        // when
        GetReviewResponse response = reviewService.getReview(1L);

        // then
        assertNotNull(response);
        assertEquals(response.reviewId(), review.getReviewId());
        assertEquals(response.classId(), review.getOneDayClass().getOneDayClassId());
        assertEquals(response.className(), review.getOneDayClass().getClassName());
        assertEquals(response.lessonId(), review.getLesson().getLessonId());
        assertEquals(response.userId(), review.getUser().getUserId());
        assertEquals(response.userNickName(), review.getUser().getNickname());
        assertEquals(response.rating(), review.getRating());
        assertEquals(response.contents(), review.getContents());
        assertEquals(response.lessonDate(), review.getLesson().getLessonDate());
        assertEquals(response.createdAt(), review.getCreatedAt());
        assertThat(response.reviewImageUrlList()).containsExactly("url1", "url2", "url3");
    }

    @Test
    @DisplayName("리뷰 조회 실패 - 존재하지 않는 리뷰")
    void getReview_fail_notExistReview() {
        // given
        given(reviewRepository.findById(1L)).willReturn(Optional.empty());

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> reviewService.getReview(1L));

        // then
        assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 등록 성공")
    void createReview_success() {
        // given
        String url1 = "url1";
        String url2 = "url2";
        String url3 = "url3";

        RegisterReviewDto.Request request = createRegisterReviewDtoRequest();

        Review reviewToSave = RegisterReviewDto.Request.toEntity(mockUser, mockLesson,
                mockOneDayClass, request);
        Review savedReview = Review.builder()
                .reviewId(1L)
                .user(mockUser)
                .lesson(mockLesson)
                .oneDayClass(mockOneDayClass)
                .contents(request.contents())
                .rating(request.rating())
                .build();

        given(reviewRepository.findByLessonAndUser(mockLesson, mockUser))
                .willReturn(Optional.empty());
        given(lessonService.findLessonById(1L)).willReturn(mockLesson);
        given(s3Service.uploadReviewImage(request.image1())).willReturn(url1);
        given(s3Service.uploadReviewImage(request.image2())).willReturn(url2);
        given(s3Service.uploadReviewImage(request.image3())).willReturn(url3);
        given(reviewRepository.save(reviewToSave)).willReturn(savedReview);

        // when
        RegisterReviewDto.Response response = reviewService.registerReview(mockUser, request);

        // then
        assertNotNull(response);
        assertEquals(response.reviewId(), savedReview.getReviewId());

        verify(reviewRepository, times(1)).save(reviewToSave);
        verify(s3Service, times(1)).uploadReviewImage(request.image1());
        verify(s3Service, times(1)).uploadReviewImage(request.image2());
        verify(s3Service, times(1)).uploadReviewImage(request.image3());

        ArgumentCaptor<ReviewImage> reviewImageCaptor = ArgumentCaptor.forClass(ReviewImage.class);
        verify(reviewImageRepository, times(3)).save(reviewImageCaptor.capture());

        List<ReviewImage> capturedReviewImages = reviewImageCaptor.getAllValues();
        assertThat(capturedReviewImages).extracting("url").containsExactly(url1, url2, url3);
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 이미 리뷰를 등록한 사용자")
    void createReview_fail_alreadyExistsReview() {
        // given
        RegisterReviewDto.Request request = createRegisterReviewDtoRequest();

        given(lessonService.findLessonById(request.lessonId())).willReturn(mockLesson);
        given(reviewRepository.findByLessonAndUser(mockLesson, mockUser))
                .willReturn(Optional.of(Review.builder().reviewId(1L).build()));

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> reviewService.registerReview(mockUser, request));

        // then
        assertEquals(ErrorCode.REVIEW_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 평점 범위 초과")
    void createReview_fail_invalidRating() {
        // given
        RegisterReviewDto.Request request = new Request(1L, 1L, "valid contents", 5.1,
                mock(MultipartFile.class), mock(MultipartFile.class), mock(MultipartFile.class));

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> reviewService.registerReview(mockUser, request));

        // then
        assertEquals(INVALID_REVIEW_RATING, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 리뷰 내용 길이 부족")
    void createReview_fail_invalidReviewContents() {
        // given
        RegisterReviewDto.Request request = new Request(1L, 1L, "short", 4.5,
                mock(MultipartFile.class), mock(MultipartFile.class), mock(MultipartFile.class));

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> reviewService.registerReview(mockUser, request));

        // then
        assertEquals(INVALID_REVIEW_CONTENTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 클래스 정보 일치 하지 않음")
    void createReview_fail_notMatchClass() {
        // given
        RegisterReviewDto.Request request = createRegisterReviewDtoRequest();

        mockOneDayClass = OneDayClass.builder()
                .oneDayClassId(2L)
                .build();
        mockLesson = Lesson.builder()
                .lessonId(1L)
                .oneDayClass(mockOneDayClass)
                .build();

        given(lessonService.findLessonById(request.lessonId()))
                .willReturn(mockLesson);

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> reviewService.registerReview(mockUser, request));

        // then
        assertEquals(INVALID_ONE_DAY_CLASS_ID, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReview_success() {
        // given
        Long reviewId = 1L;

        UpdateReviewDto.Request request = createUpdateReviewDtoRequest();

        Review savedReview = Review.builder()
                .reviewId(reviewId)
                .user(mockUser)
                .lesson(mockLesson)
                .oneDayClass(mockOneDayClass)
                .contents("This is a contents.")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .reviewImageList(List.of(
                        ReviewImage.builder().url("url1").build(),
                        ReviewImage.builder().url("url2").build(),
                        ReviewImage.builder().url("url3").build()
                ))
                .build();

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));

        // when
        UpdateReviewDto.Response response = reviewService.updateReview(mockUser, request, reviewId);

        // then
        assertThat(response).isNotNull();
        assertEquals(response.reviewId(), savedReview.getReviewId());
        assertEquals(response.contents(), request.contents());
        assertEquals(response.rating(), request.rating());

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(s3Service, times(1)).uploadReviewImage(request.image1());
        verify(s3Service, times(1)).uploadReviewImage(request.image2());
        verify(s3Service, times(1)).uploadReviewImage(request.image3());
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 존재하지 않는 리뷰")
    void updateReview_fail_notExistReview() {
        // given
        Long reviewId = 1L;
        UpdateReviewDto.Request request = createUpdateReviewDtoRequest();

        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> reviewService.updateReview(mockUser, request, reviewId));

        // then
        assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 리뷰 작성자가 아닌 사용자")
    void updateReview_fail_notReviewOwner() {
        // given
        Long reviewId = 1L;
        UpdateReviewDto.Request request = createUpdateReviewDtoRequest();

        Review savedReview = Review.builder()
                .reviewId(reviewId)
                .user(User.builder().userId(2L).build())
                .build();

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> reviewService.updateReview(mockUser, request, reviewId));

        // then
        assertEquals(ErrorCode.NOT_REVIEW_OWNER, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 평점 범위 초과")
    void updateReview_fail_invalidRating() {
        // given
        Long reviewId = 1L;
        UpdateReviewDto.Request request = new UpdateReviewDto.Request(
                "This is a update contents.", 5.1,
                mock(MultipartFile.class), mock(MultipartFile.class), mock(MultipartFile.class));

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> reviewService.updateReview(mockUser, request, reviewId));

        // then
        assertEquals(INVALID_REVIEW_RATING, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 리뷰 내용 길이 부족")
    void updateReview_fail_invalidReviewContents() {
        // given
        Long reviewId = 1L;
        UpdateReviewDto.Request request = new UpdateReviewDto.Request(
                "short", 3.0,
                mock(MultipartFile.class), mock(MultipartFile.class), mock(MultipartFile.class));

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> reviewService.updateReview(mockUser, request, reviewId));

        // then
        assertEquals(INVALID_REVIEW_CONTENTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReview_success() {
        // given
        Long reviewId = 1L;

        Review savedReview = Review.builder()
                .reviewId(reviewId)
                .user(mockUser)
                .lesson(mockLesson)
                .oneDayClass(mockOneDayClass)
                .contents("This is a contents.")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .reviewImageList(List.of(
                        ReviewImage.builder().url("url1").build(),
                        ReviewImage.builder().url("url2").build(),
                        ReviewImage.builder().url("url3").build()
                ))
                .build();

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));
        given(reviewImageRepository.findByReviewOrderBySequenceAsc(savedReview))
                .willReturn(savedReview.getReviewImageList());

        // when
        DeleteReviewResponse response = reviewService.deleteReview(mockUser, reviewId);

        // then
        assertEquals(response.reviewId(), reviewId);

        verify(reviewRepository, times(1)).delete(savedReview);
        verify(s3Service, times(1)).delete("url1");
        verify(s3Service, times(1)).delete("url2");
        verify(s3Service, times(1)).delete("url3");
    }

    private RegisterReviewDto.Request createRegisterReviewDtoRequest() {
        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        MultipartFile image3 = mock(MultipartFile.class);
        return new RegisterReviewDto.Request(
                mockLesson.getLessonId(), mockOneDayClass.getOneDayClassId(),
                "This is a contents.", 4.5, image1, image2, image3);
    }

    private UpdateReviewDto.Request createUpdateReviewDtoRequest() {
        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        MultipartFile image3 = mock(MultipartFile.class);
        return new UpdateReviewDto.Request(
                "This is a update contents.", 3.0, image1, image2, image3);
    }
}