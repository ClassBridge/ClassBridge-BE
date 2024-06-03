package com.linked.classbridge.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.oneDayClass.ClassDto;
import com.linked.classbridge.dto.oneDayClass.ClassUpdateDto;
import com.linked.classbridge.dto.review.GetReviewResponse;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.service.ClassService;
import com.linked.classbridge.service.ReviewService;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tutors")
public class TutorController {

    private final ReviewService reviewService;
    private final ClassService classService;
    private final UserRepository userRepository;

    @Operation(summary = "강사 리뷰 조회", description = "강사 리뷰 조회")
    @GetMapping("/reviews")
    public ResponseEntity<SuccessResponse<Page<GetReviewResponse>>> getClassReviews(
            @PageableDefault Pageable pageable
    ) {

        User tutor = userRepository.findById(1L).orElse(null);

        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.REVIEW_GET_SUCCESS,
                        reviewService.getTutorReviews(tutor, pageable)
                )
        );
    }

    /**
     * 강사 클래스 리스트 조회
     * @param pageable
     * @return ResponseEntity<SuccessResponse<Page<ClassDto>>
     */
    @Operation(summary = "Class list 조회", description = "Class list 조회")
    @GetMapping
    public ResponseEntity<SuccessResponse<Page<ClassDto>>> getOneDayClassList(/* Authentication authentication, */ Pageable pageable) {
        return ResponseEntity.status(OK).body(SuccessResponse.of(
                ResponseMessage.ONE_DAY_CLASS_LIST_GET_SUCCESS,
                classService.getOneDayClassList(/*authentication, */ pageable))
        );
    }

    /**
     * 강사 클래스 조회
     * @param
     * @return ResponseEntity<SuccessResponse<ClassDto.Response>
     */
    @Operation(summary = "Class 조회", description = "Class 조회")
    @GetMapping("/{classId}")
    public ResponseEntity<SuccessResponse<ClassDto.ClassResponse>> getOneDayClass(/*Authentication authentication, */
            @PathVariable String classId) {
        return ResponseEntity.status(OK).body(SuccessResponse.of(
                ResponseMessage.ONE_DAY_CLASS_LIST_GET_SUCCESS,
                classService.getOneDayClass(/*authentication, */ Long.parseLong(classId)))
        );
    }

    /**
     * Class 등록
     * @param   request, file1, file2, file3, file4, file5
     * @return  ResponseEntity<SuccessResponse<ClassDto>>
     */
    @Operation(summary = "Class 등록", description = "Class 등록")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<ClassDto.ClassResponse>> registerClass(
            /* Authentication authentication, */
            @RequestPart(value = "request") @Valid ClassDto.ClassRequest request,
            @RequestPart(value = "file1", required = false) MultipartFile file1,
            @RequestPart(value = "file2", required = false) MultipartFile file2,
            @RequestPart(value = "file3", required = false) MultipartFile file3,
            @RequestPart(value = "file4", required = false) MultipartFile file4,
            @RequestPart(value = "file5", required = false) MultipartFile file5
    ) {
        List<MultipartFile> fileList = Arrays.stream((new MultipartFile[] {file1, file2, file3, file4, file5}))
                .filter(item -> item != null && !item.isEmpty()).toList();

        User user = userRepository.findById(1L).orElse(userRepository.save(User.builder().email("example@example.com").password("1234").nickname("닉네임").build()));

        return ResponseEntity.status(CREATED).body(SuccessResponse.of(
                ResponseMessage.CLASS_REGISTER_SUCCESS,
                classService.registerClass(user, request, fileList))
        );
    }

    /**
     * Class 정보 수정
     * @param   request
     * @return  ResponseEntity<SuccessResponse<ClassDto>>
     */
    @Operation(summary = "Class 정보 수정", description = "Class 정보 수정")
    @PostMapping(path = "/{classId}")
    public ResponseEntity<SuccessResponse<ClassUpdateDto.ClassResponse>> updateClass(
            /* Authentication authentication, */
            @PathVariable String classId,
            @RequestBody @Valid ClassUpdateDto.ClassRequest request
    ) throws IOException {
        return ResponseEntity.status(CREATED).body(SuccessResponse.of(
                ResponseMessage.CLASS_REGISTER_SUCCESS,
                classService.updateClass(request, Long.parseLong(classId)))
        );
    }

    /**
     * Class 삭제
     * @param   classId
     * @return  ResponseEntity<SuccessResponse<ClassDto>>
     */
    @Operation(summary = "Class 삭제", description = "Class 삭제")
    @DeleteMapping(path = "/{classId}")
    public ResponseEntity<SuccessResponse<Boolean>> updateClass(
            /* Authentication authentication, */
            @PathVariable String classId
    ) throws IOException {
        return ResponseEntity.status(OK).body(SuccessResponse.of(
                ResponseMessage.CLASS_DELETE_SUCCESS,
                classService.deleteClass(Long.parseLong(classId)))
        );
    }

}
