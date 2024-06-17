package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.CANNOT_ADD_WISH_OWN_CLASS;
import static com.linked.classbridge.type.ErrorCode.EXISTS_WISH_CLASS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.ClassImage;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.Wish;
import com.linked.classbridge.domain.document.OneDayClassDocument;
import com.linked.classbridge.dto.user.WishDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.CategoryRepository;
import com.linked.classbridge.repository.ClassImageRepository;
import com.linked.classbridge.repository.OneDayClassDocumentRepository;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.repository.WishRepository;
import com.linked.classbridge.type.CategoryType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = "spring.config.location=classpath:application-test.yml")
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private S3Service s3Service;

    @Mock
    private OneDayClassRepository oneDayClassRepository;

    @Mock
    private WishRepository wishRepository;

    @Mock
    private ClassImageRepository classImageRepository;

    @Mock
    private OneDayClassDocumentRepository oneDayClassDocumentRepository;

    @Mock
    private ElasticsearchOperations operations;

    @Test
    @WithMockUser
    void getWishList() {
        // Given
        User user = User.builder().userId(1L).email("example@example.com").build();
        User tutor = User.builder().userId(2L).email("example1@example.com").build();
        Category category = Category.builder().name(CategoryType.FITNESS).build();

        OneDayClass oneDayClass1 = OneDayClass.builder()
                .classId(1L)
                .className("클래스")
                .totalWish(1)
                .totalReviews(0)
                .totalStarRate(0.0)
                .personal(5)
                .duration(60)
                .address1("서울특별시")
                .address2("강남구")
                .address3("세부주소 ...")
                .category(category)
                .tutor(tutor).build();
        Wish wish1 = Wish.builder().user(user).oneDayClass(oneDayClass1).id(1L).build();

        OneDayClass oneDayClass2 = OneDayClass.builder()
                .classId(2L)
                .className("클래스")
                .totalWish(1)
                .totalReviews(0)
                .totalStarRate(0.0)
                .personal(5)
                .duration(60)
                .address1("경기")
                .address2("성남시")
                .address3("세부주소 ...")
                .category(category).
                tutor(tutor).build();
        Wish wish2 = Wish.builder().user(user).oneDayClass(oneDayClass2).id(2L).build();

        OneDayClass oneDayClass3 = OneDayClass.builder()
                .classId(3L)
                .className("클래스")
                .totalWish(1)
                .totalReviews(0)
                .totalStarRate(0.0)
                .personal(5)
                .duration(60)
                .address1("서울특별시")
                .address2("중구")
                .address3("세부주소 ...")
                .category(category)
                .tutor(tutor).build();
        Wish wish3 = Wish.builder().user(user).oneDayClass(oneDayClass3).id(3L).build();

        ClassImage classImage1 = ClassImage.builder().classImageId(1L).url("url1").oneDayClass(oneDayClass1).sequence(1).build();
        ClassImage classImage2 = ClassImage.builder().classImageId(2L).url("url2").oneDayClass(oneDayClass2).sequence(1).build();
        ClassImage classImage3 = ClassImage.builder().classImageId(3L).url("url3").oneDayClass(oneDayClass3).sequence(1).build();

        List<Wish> wishList = Arrays.asList(wish1, wish2, wish3);
        List<OneDayClass> classList = Arrays.asList(oneDayClass1, oneDayClass2, oneDayClass3);
        List<ClassImage> imageList = Arrays.asList(classImage1, classImage2, classImage3);

        WishDto wishDto1 = new WishDto(oneDayClass1);
        WishDto wishDto2 = new WishDto(oneDayClass2);
        WishDto wishDto3 = new WishDto(oneDayClass3);

        wishDto1.setWishId(1L);
        wishDto2.setWishId(2L);
        wishDto3.setWishId(3L);

        wishDto1.setClassImageUrl(classImage1.getUrl());
        wishDto2.setClassImageUrl(classImage2.getUrl());
        wishDto3.setClassImageUrl(classImage3.getUrl());

        Pageable pageable = mock(Pageable.class);

        Page<OneDayClass> classPage = new PageImpl<>(classList, pageable, 3);

        given(userService.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(wishRepository.findByUserUserId(user.getUserId())).willReturn(wishList);
        given(oneDayClassRepository.findAllByClassIdIn(wishList.stream().map(wish -> wish.getOneDayClass().getClassId()).toList(), pageable)).willReturn(classPage);
        given(classImageRepository.findAllByOneDayClassClassIdInAndSequence(classList.stream().map(OneDayClass::getClassId).toList(), 1)).willReturn(imageList);

        // when
        Page<WishDto> response = userService.getWishList(user.getEmail(), pageable);

        assertNotNull(response);
        assertEquals(response.getContent().size(), 3);
        assertEquals(response.getContent().get(0).getWishId(), wishDto1.getWishId());
        assertEquals(response.getContent().get(1).getWishId(), wishDto2.getWishId());
        assertEquals(response.getContent().get(2).getWishId(), wishDto3.getWishId());
    }

    @Test
    @WithMockUser
    void addWish() {
        User user = User.builder().userId(1L).email("example@example.com").build();
        User tutor = User.builder().userId(2L).email("example1@example.com").build();
        Category category = Category.builder().name(CategoryType.FITNESS).build();

        OneDayClass oneDayClass = OneDayClass.builder()
                .classId(1L)
                .className("클래스")
                .totalWish(1)
                .totalReviews(0)
                .totalStarRate(0.0)
                .personal(5)
                .duration(60)
                .address1("서울특별시")
                .address2("강남구")
                .address3("세부주소 ...")
                .category(category)
                .tutor(tutor).build();

        OneDayClassDocument oneDayClassDocument = OneDayClassDocument.builder().classId(1L).totalWish(0).build();

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(oneDayClassRepository.findById(oneDayClass.getClassId())).willReturn(Optional.of(oneDayClass));
        given(wishRepository.existsByUserUserIdAndOneDayClassClassId(user.getUserId(), oneDayClass.getClassId())).willReturn(false);
        given(oneDayClassDocumentRepository.findById(oneDayClass.getClassId())).willReturn(Optional.of(oneDayClassDocument));

        boolean response = userService.addWish(user.getEmail(), oneDayClass.getClassId());

        assertTrue(response);
    }

    @Test
    @WithMockUser
    void addWish_fail_tutor_cant_add_tutor_class() {
        User tutor = User.builder().userId(2L).email("example1@example.com").build();
        Category category = Category.builder().name(CategoryType.FITNESS).build();

        OneDayClass oneDayClass = OneDayClass.builder()
                .classId(1L)
                .className("클래스")
                .totalWish(1)
                .totalReviews(0)
                .totalStarRate(0.0)
                .personal(5)
                .duration(60)
                .address1("서울특별시")
                .address2("강남구")
                .address3("세부주소 ...")
                .category(category)
                .tutor(tutor).build();

        given(userRepository.findByEmail(tutor.getEmail())).willReturn(Optional.of(tutor));
        given(oneDayClassRepository.findById(oneDayClass.getClassId())).willReturn(Optional.of(oneDayClass));

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> userService.addWish(tutor.getEmail(), oneDayClass.getClassId()));

        // then
        assertEquals(CANNOT_ADD_WISH_OWN_CLASS, exception.getErrorCode());
    }

    @Test
    @WithMockUser
    void addWish_fail_exists_class() {
        User tutor = User.builder().userId(2L).email("example1@example.com").build();
        User user = User.builder().userId(1L).email("example@example.com").build();
        Category category = Category.builder().name(CategoryType.FITNESS).build();

        OneDayClass oneDayClass = OneDayClass.builder()
                .classId(1L)
                .className("클래스")
                .totalWish(1)
                .totalReviews(0)
                .totalStarRate(0.0)
                .personal(5)
                .duration(60)
                .address1("서울특별시")
                .address2("강남구")
                .address3("세부주소 ...")
                .category(category)
                .tutor(tutor).build();

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(oneDayClassRepository.findById(oneDayClass.getClassId())).willReturn(Optional.of(oneDayClass));
        given(wishRepository.existsByUserUserIdAndOneDayClassClassId(user.getUserId(), oneDayClass.getClassId())).willReturn(true);

        // when
        RestApiException exception = assertThrows(RestApiException.class,
                () -> userService.addWish(user.getEmail(), oneDayClass.getClassId()));

        // then
        assertEquals(EXISTS_WISH_CLASS, exception.getErrorCode());
    }

    @Test
    @WithMockUser
    void deleteWish() {
        User user = User.builder().userId(1L).email("example@example.com").build();
        User tutor = User.builder().userId(2L).email("example1@example.com").build();
        Category category = Category.builder().name(CategoryType.FITNESS).build();


        OneDayClass oneDayClass = OneDayClass.builder()
                .classId(1L)
                .className("클래스")
                .totalWish(1)
                .totalReviews(0)
                .totalStarRate(0.0)
                .personal(5)
                .duration(60)
                .address1("서울특별시")
                .address2("강남구")
                .address3("세부주소 ...")
                .category(category)
                .tutor(tutor).build();

        Wish wish = Wish.builder().user(user).oneDayClass(oneDayClass).id(1L).build();
        OneDayClassDocument oneDayClassDocument = OneDayClassDocument.builder().classId(1L).totalWish(0).build();

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(oneDayClassRepository.findById(oneDayClass.getClassId())).willReturn(Optional.of(oneDayClass));
        given(wishRepository.findById(wish.getId())).willReturn(Optional.of(wish));
        given(oneDayClassDocumentRepository.findById(oneDayClass.getClassId())).willReturn(Optional.of(oneDayClassDocument));

        // when
        boolean response = userService.deleteWish(user.getEmail(), wish.getId());

        //then
        assertTrue(response);
    }
}