package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.CLASS_NOT_FOUND;
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
import com.linked.classbridge.domain.document.OneDayClassDocument;
import com.linked.classbridge.dto.review.DeleteReviewResponse;
import com.linked.classbridge.dto.review.GetReviewResponse;
import com.linked.classbridge.dto.review.RegisterReviewDto;
import com.linked.classbridge.dto.review.RegisterReviewDto.Request;
import com.linked.classbridge.dto.review.UpdateReviewDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.OneDayClassDocumentRepository;
import com.linked.classbridge.repository.ReviewImageRepository;
import com.linked.classbridge.repository.ReviewRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private OneDayClassService oneDayClassService;
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LessonService lessonService;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private OneDayClassDocumentRepository oneDayClassDocumentRepository;

    @Mock
    private ElasticsearchOperations operations;

    private User mockUser1;
    private User mockUser2;
    private User tutor;
    private Lesson mockLesson1;
    private Lesson mockLesson2;
    private OneDayClass mockOneDayClass1;
    private OneDayClass mockOneDayClass2;

    private Review mockReview1;
    private Review mockReview2;
    private Review mockReview3;
    private Review mockReview4;

    private ReviewImage mockReviewImage1;
    private ReviewImage mockReviewImage2;
    private ReviewImage mockReviewImage3;
    private ReviewImage mockReviewImage4;
    private ReviewImage mockReviewImage5;
    private ReviewImage mockReviewImage6;

    @BeforeEach
    void setUp() {
        mockUser1 = User.builder()
                .userId(1L)
                .nickname("user1")
                .reviewList(new ArrayList<>())
                .build();
        mockUser2 = User.builder()
                .userId(2L)
                .nickname("user2")
                .reviewList(new ArrayList<>())
                .build();

        tutor = User.builder()
                .userId(3L)
                .nickname("tutor")
                .reviewList(new ArrayList<>())
                .build();

        mockOneDayClass1 = OneDayClass.builder()
                .classId(1L)
                .tutor(tutor)
                .className("oneDayClassName")
                .reviewList(new ArrayList<>())
                .totalStarRate(0.0)
                .totalReviews(0)
                .build();
        mockOneDayClass2 = OneDayClass.builder()
                .classId(2L)
                .tutor(tutor)
                .className("oneDayClassName2")
                .reviewList(new ArrayList<>())
                .totalStarRate(0.0)
                .totalReviews(0)
                .build();
        mockLesson1 = Lesson.builder()
                .lessonId(1L)
                .oneDayClass(mockOneDayClass1)
                .lessonDate(LocalDate.now())
                .reviewList(new ArrayList<>())
                .build();
        mockLesson2 = Lesson.builder()
                .lessonId(2L)
                .oneDayClass(mockOneDayClass2)
                .lessonDate(LocalDate.now())
                .reviewList(new ArrayList<>())
                .build();
        mockReview1 = Review.builder()
                .reviewId(1L)
                .user(mockUser1)
                .lesson(mockLesson1)
                .oneDayClass(mockOneDayClass1)
                .contents("review1 contents")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .reviewImageList(new ArrayList<>())
                .build();
        mockReview2 = Review.builder()
                .reviewId(2L)
                .user(mockUser2)
                .lesson(mockLesson1)
                .oneDayClass(mockOneDayClass1)
                .contents("review2 contents")
                .rating(3.5)
                .createdAt(LocalDateTime.now())
                .reviewImageList(new ArrayList<>())
                .build();
        mockReview3 = Review.builder()
                .reviewId(3L)
                .user(mockUser1)
                .lesson(mockLesson2)
                .oneDayClass(mockOneDayClass2)
                .contents("review3 contents")
                .rating(2.5)
                .createdAt(LocalDateTime.now())
                .reviewImageList(new ArrayList<>())
                .build();
        mockReview4 = Review.builder()
                .reviewId(4L)
                .user(mockUser2)
                .lesson(mockLesson2)
                .oneDayClass(mockOneDayClass2)
                .contents("review4 contents")
                .rating(1.5)
                .createdAt(LocalDateTime.now())
                .reviewImageList(new ArrayList<>())
                .build();
        mockReviewImage1 = ReviewImage.builder()
                .reviewImageId(1L)
                .review(mockReview1)
                .url("url1")
                .sequence(1)
                .build();
        mockReviewImage2 = ReviewImage.builder()
                .reviewImageId(2L)
                .review(mockReview1)
                .url("url2")
                .sequence(2)
                .build();
        mockReviewImage3 = ReviewImage.builder()
                .reviewImageId(3L)
                .review(mockReview1)
                .url("url3")
                .sequence(1)
                .build();
        mockReviewImage4 = ReviewImage.builder()
                .reviewImageId(4L)
                .review(mockReview2)
                .url("url4")
                .sequence(2)
                .build();
        mockReviewImage5 = ReviewImage.builder()
                .reviewImageId(5L)
                .review(mockReview2)
                .url("url5")
                .sequence(2)
                .build();
        mockReviewImage6 = ReviewImage.builder()
                .reviewImageId(6L)
                .review(mockReview2)
                .url("url6")
                .sequence(3)
                .build();
        mockUser1.addReview(mockReview1);
        mockUser1.addReview(mockReview3);
        mockUser2.addReview(mockReview2);
        mockUser2.addReview(mockReview4);
        mockLesson1.addReview(mockReview1);
        mockLesson1.addReview(mockReview2);
        mockLesson2.addReview(mockReview3);
        mockLesson2.addReview(mockReview4);
        mockOneDayClass1.addReview(mockReview1);
        mockOneDayClass1.addReview(mockReview2);
        mockOneDayClass2.addReview(mockReview3);
        mockOneDayClass2.addReview(mockReview4);
        mockReview1.addReviewImage(mockReviewImage1);
        mockReview1.addReviewImage(mockReviewImage2);
        mockReview1.addReviewImage(mockReviewImage3);
        mockReview2.addReviewImage(mockReviewImage4);
        mockReview2.addReviewImage(mockReviewImage5);
        mockReview2.addReviewImage(mockReviewImage6);
    }

    private RegisterReviewDto.Request createRegisterReviewDtoRequest() {
        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        MultipartFile image3 = mock(MultipartFile.class);
        return new RegisterReviewDto.Request(
                mockLesson1.getLessonId(), mockOneDayClass1.getClassId(),
                mockReview1.getContents(), mockReview1.getRating(), image1, image2, image3);
    }

    private UpdateReviewDto.Request createUpdateReviewDtoRequest() {
        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        MultipartFile image3 = mock(MultipartFile.class);
        return new UpdateReviewDto.Request(
                "This is a update contents.", 3.0, image1, image2, image3);
    }

    @Test
    @DisplayName("리뷰 조회 성공")
    void getReview_success() {
        // given
        given(reviewRepository.findById(1L)).willReturn(Optional.of(mockReview1));

        // when
        GetReviewResponse response = reviewService.getReview(1L);

        // then
        assertNotNull(response);
        assertEquals(response.reviewId(), mockReview1.getReviewId());
        assertEquals(response.classId(), mockReview1.getOneDayClass().getClassId());
        assertEquals(response.className(), mockReview1.getOneDayClass().getClassName());
        assertEquals(response.lessonId(), mockReview1.getLesson().getLessonId());
        assertEquals(response.userId(), mockReview1.getUser().getUserId());
        assertEquals(response.userNickName(), mockReview1.getUser().getNickname());
        assertEquals(response.rating(), mockReview1.getRating());
        assertEquals(response.contents(), mockReview1.getContents());
        assertEquals(response.lessonDate(), mockReview1.getLesson().getLessonDate());
        assertEquals(response.createdAt(), mockReview1.getCreatedAt());
        assertThat(response.reviewImageUrlList()).containsExactly(
                mockReview1.getReviewImageList().get(0).getUrl(),
                mockReview1.getReviewImageList().get(1).getUrl(),
                mockReview1.getReviewImageList().get(2).getUrl());
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
        String url1 = mockReviewImage1.getUrl();
        String url2 = mockReviewImage2.getUrl();
        String url3 = mockReviewImage3.getUrl();

        RegisterReviewDto.Request request = createRegisterReviewDtoRequest();

        Review reviewToSave = RegisterReviewDto.Request.toEntity(mockUser1, mockLesson1,
                mockOneDayClass1, request);
        Review savedReview = mockReview1;

        OneDayClassDocument oneDayClassDocument = OneDayClassDocument.builder().classId(1L).build();

        given(reviewRepository.findByLessonAndUser(mockLesson1, mockUser1))
                .willReturn(Optional.empty());
        given(lessonService.findLessonById(1L)).willReturn(mockLesson1);
        given(s3Service.uploadReviewImage(request.image1())).willReturn(url1);
        given(s3Service.uploadReviewImage(request.image2())).willReturn(url2);
        given(s3Service.uploadReviewImage(request.image3())).willReturn(url3);
        given(reviewRepository.save(reviewToSave)).willReturn(savedReview);

        given(oneDayClassDocumentRepository.findById(mockOneDayClass1.getClassId())).willReturn(Optional.of(oneDayClassDocument));

        // when
        RegisterReviewDto.Response response = reviewService.registerReview(mockUser1, request);

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

        given(lessonService.findLessonById(request.lessonId())).willReturn(mockLesson1);
        given(reviewRepository.findByLessonAndUser(mockLesson1, mockUser1))
                .willReturn(Optional.of(Review.builder().reviewId(1L).build()));

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> reviewService.registerReview(mockUser1, request));

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
                () -> reviewService.registerReview(mockUser1, request));

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
                () -> reviewService.registerReview(mockUser1, request));

        // then
        assertEquals(INVALID_REVIEW_CONTENTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 클래스 정보 일치 하지 않음")
    void createReview_fail_notMatchClass() {
        // given
        RegisterReviewDto.Request request = createRegisterReviewDtoRequest();

        mockOneDayClass1 = OneDayClass.builder()
                .classId(2L)
                .build();
        mockLesson1 = Lesson.builder()
                .lessonId(1L)
                .oneDayClass(mockOneDayClass1)
                .build();

        given(lessonService.findLessonById(request.lessonId()))
                .willReturn(mockLesson1);

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> reviewService.registerReview(mockUser1, request));

        // then
        assertEquals(INVALID_ONE_DAY_CLASS_ID, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReview_success() {
        // given
        Long reviewId = 1L;

        UpdateReviewDto.Request request = createUpdateReviewDtoRequest();
        OneDayClassDocument oneDayClassDocument = OneDayClassDocument.builder().classId(1L).build();
        Review savedReview = mockReview1;

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));
        given(oneDayClassDocumentRepository.findById(mockOneDayClass1.getClassId())).willReturn(Optional.of(oneDayClassDocument));

        // when
        UpdateReviewDto.Response response = reviewService.updateReview(mockUser1, request,
                reviewId);

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
                () -> reviewService.updateReview(mockUser1, request, reviewId));

        // then
        assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 리뷰 작성자가 아닌 사용자")
    void updateReview_fail_notReviewOwner() {
        // given
        Long reviewId = 1L;
        UpdateReviewDto.Request request = createUpdateReviewDtoRequest();

        Review savedReview = mockReview2;

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> reviewService.updateReview(mockUser1, request, reviewId));

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
                () -> reviewService.updateReview(mockUser1, request, reviewId));

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
                () -> reviewService.updateReview(mockUser1, request, reviewId));

        // then
        assertEquals(INVALID_REVIEW_CONTENTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReview_success() {
        // given
        Long reviewId = 1L;

        Review savedReview = mockReview1;

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));
        given(reviewImageRepository.findByReviewOrderBySequenceAsc(savedReview))
                .willReturn(savedReview.getReviewImageList());

        // when
        DeleteReviewResponse response = reviewService.deleteReview(mockUser1, reviewId);

        // then
        assertEquals(response.reviewId(), reviewId);

        verify(reviewRepository, times(1)).delete(savedReview);
        verify(s3Service, times(1)).delete("url1");
        verify(s3Service, times(1)).delete("url2");
        verify(s3Service, times(1)).delete("url3");
    }


    @Test
    @DisplayName("클래스 리뷰 목록 조회 성공")
    void getClassReviews_success() {
        // given
        Pageable pageable = mock(Pageable.class);

        Page<Review> reviewPage =
                new PageImpl<>(Arrays.asList(mockReview1, mockReview2), pageable, 2);

        given(oneDayClassService.findClassById(1L)).willReturn(mockOneDayClass1);
        given(reviewRepository.findByOneDayClass(mockOneDayClass1, pageable))
                .willReturn(reviewPage);

        // when
        Page<GetReviewResponse> responses = reviewService.getClassReviews(1L, pageable);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("reviewId").containsExactly(
                mockReview1.getReviewId(), mockReview2.getReviewId());
        assertThat(responses).extracting("classId").containsExactly(
                mockOneDayClass1.getClassId(), mockOneDayClass1.getClassId());
        assertThat(responses).extracting("className").containsExactly(
                mockOneDayClass1.getClassName(), mockOneDayClass1.getClassName());
        assertThat(responses).extracting("lessonId").containsExactly(
                mockLesson1.getLessonId(), mockLesson1.getLessonId());
        assertThat(responses).extracting("tutorId").containsExactly(
                mockUser1.getUserId(), mockUser2.getUserId());
        assertThat(responses).extracting("userNickName").containsExactly(
                mockUser1.getNickname(), mockUser2.getNickname());
        assertThat(responses).extracting("rating").containsExactly(
                mockReview1.getRating(), mockReview2.getRating());
        assertThat(responses).extracting("contents").containsExactly(
                mockReview1.getContents(), mockReview2.getContents());
        assertThat(responses).extracting("lessonDate")
                .containsExactly(mockLesson1.getLessonDate(), mockLesson1.getLessonDate());
    }

    @Test
    @DisplayName("클래스 리뷰 목록 조회 실패 - 존재하지 않는 클래스")
    void getClassReviews_fail_notExistClass() {
        // given
        Pageable pageable = mock(Pageable.class);

        given(oneDayClassService.findClassById(1L))
                .willThrow(new RestApiException(CLASS_NOT_FOUND));

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> reviewService.getClassReviews(1L, pageable));

        // then
        assertEquals(CLASS_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("유저 리뷰 목록 조회 성공")
    void getUserReviews_success() {
        // given
        Pageable pageable = mock(Pageable.class);

        Page<Review> reviewPage =
                new PageImpl<>(Arrays.asList(mockReview1, mockReview3), pageable, 2);

        given(reviewRepository.findByUser(mockUser1, pageable)).willReturn(reviewPage);

        // when
        Page<GetReviewResponse> responses = reviewService.getUserReviews(mockUser1, pageable);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("reviewId").containsExactly(
                mockReview1.getReviewId(), mockReview3.getReviewId());
        assertThat(responses).extracting("classId").containsExactly(
                mockOneDayClass1.getClassId(), mockOneDayClass2.getClassId());
        assertThat(responses).extracting("className").containsExactly(
                mockOneDayClass1.getClassName(), mockOneDayClass2.getClassName());
        assertThat(responses).extracting("lessonId").containsExactly(
                mockLesson1.getLessonId(), mockLesson2.getLessonId());
        assertThat(responses).extracting("tutorId").containsExactly(
                mockUser1.getUserId(), mockUser1.getUserId());
        assertThat(responses).extracting("userNickName").containsExactly(
                mockUser1.getNickname(), mockUser1.getNickname());
        assertThat(responses).extracting("rating").containsExactly(
                mockReview1.getRating(), mockReview3.getRating());
        assertThat(responses).extracting("contents").containsExactly(
                mockReview1.getContents(), mockReview3.getContents());
        assertThat(responses).extracting("lessonDate")
                .containsExactly(mockLesson1.getLessonDate(), mockLesson2.getLessonDate());
    }

    @Test
    @DisplayName("강사 받은 리뷰 목록 조회 성공")
    void getTutorReviews_success() {
        // given
        Pageable pageable = mock(Pageable.class);

        Page<Review> reviewPage =
                new PageImpl<>(Arrays.asList(mockReview1, mockReview2, mockReview3, mockReview4),
                        pageable, 4);

        given(reviewRepository.findByTutor(tutor, pageable)).willReturn(reviewPage);
        given(userRepository.findByEmail(tutor.getEmail())).willReturn(Optional.of(tutor));
        // when
        Page<GetReviewResponse> responses = reviewService.getTutorReviews(tutor.getEmail(), pageable);

        // then
        assertThat(responses).hasSize(4);
        assertThat(responses).extracting("reviewId").containsExactly(
                mockReview1.getReviewId(), mockReview2.getReviewId(),
                mockReview3.getReviewId(), mockReview4.getReviewId());
        assertThat(responses).extracting("classId").containsExactly(
                mockOneDayClass1.getClassId(), mockOneDayClass1.getClassId(),
                mockOneDayClass2.getClassId(), mockOneDayClass2.getClassId());
        assertThat(responses).extracting("className").containsExactly(
                mockOneDayClass1.getClassName(), mockOneDayClass1.getClassName(),
                mockOneDayClass2.getClassName(), mockOneDayClass2.getClassName());
        assertThat(responses).extracting("lessonId").containsExactly(
                mockLesson1.getLessonId(), mockLesson1.getLessonId()
                , mockLesson2.getLessonId(), mockLesson2.getLessonId());
        assertThat(responses).extracting("tutorId").containsExactly(
                mockUser1.getUserId(), mockUser2.getUserId(),
                mockUser1.getUserId(), mockUser2.getUserId());
        assertThat(responses).extracting("userNickName").containsExactly(
                mockUser1.getNickname(), mockUser2.getNickname(),
                mockUser1.getNickname(), mockUser2.getNickname());
        assertThat(responses).extracting("rating").containsExactly(
                mockReview1.getRating(), mockReview2.getRating(),
                mockReview3.getRating(), mockReview4.getRating());
        assertThat(responses).extracting("contents").containsExactly(
                mockReview1.getContents(), mockReview2.getContents(),
                mockReview3.getContents(), mockReview4.getContents());
        assertThat(responses).extracting("lessonDate").containsExactly(
                mockLesson1.getLessonDate(), mockLesson1.getLessonDate(),
                mockLesson2.getLessonDate(), mockLesson2.getLessonDate());
    }
}