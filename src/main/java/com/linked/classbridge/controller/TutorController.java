package com.linked.classbridge.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.oneDayClass.ClassDto;
import com.linked.classbridge.dto.oneDayClass.ClassFAQDto;
import com.linked.classbridge.dto.oneDayClass.ClassUpdateDto;
import com.linked.classbridge.dto.review.GetReviewResponse;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.service.OneDayClassService;
import com.linked.classbridge.service.ReviewService;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final OneDayClassService oneDayClassService;
    private final UserRepository userRepository;
    private final UserService userService;

    @Operation(summary = "강사 리뷰 조회", description = "강사 리뷰 조회")
    @GetMapping("/reviews")
    public ResponseEntity<SuccessResponse<Page<GetReviewResponse>>> getClassReviews(
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.REVIEW_GET_SUCCESS,
                        reviewService.getTutorReviews(userService.getCurrentUserEmail(), pageable)
                )
        );
    }

    /**
     * 강사 클래스 리스트 조회
     * @param pageable
     * @return ResponseEntity<SuccessResponse<Page<ClassDto>>
     */
    @Operation(summary = "Class list 조회", description = "Class list 조회")
    @GetMapping("/class")
    public ResponseEntity<SuccessResponse<Page<ClassDto>>> getOneDayClassList(Pageable pageable) {
        return ResponseEntity.status(OK).body(SuccessResponse.of(
                ResponseMessage.ONE_DAY_CLASS_LIST_GET_SUCCESS,
                oneDayClassService.getOneDayClassList(userService.getCurrentUserEmail(), pageable))
        );
    }

    /**
     * 강사 클래스 조회
     * @param
     * @return ResponseEntity<SuccessResponse<ClassDto.Response>
     */
    @Operation(summary = "Class 조회", description = "Class 조회")
    @GetMapping("/class/{classId}")
    public ResponseEntity<SuccessResponse<ClassDto.ClassResponse>> getOneDayClass(
            @PathVariable String classId) {
        return ResponseEntity.status(OK).body(SuccessResponse.of(
                ResponseMessage.ONE_DAY_CLASS_GET_SUCCESS,
                oneDayClassService.getOneDayClass(userService.getCurrentUserEmail(), Long.parseLong(classId)))
        );
    }

    /**
     * Class 등록
     * @param   request, file1, file2, file3, file4, file5
     * @return  ResponseEntity<SuccessResponse<ClassDto>>
     */
    @Operation(summary = "Class 등록", description = "Class 등록")
    @PostMapping(path = "/class", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<ClassDto.ClassResponse>> registerClass(
            @RequestPart(value = "request") @Valid ClassDto.ClassRequest request,
            @RequestPart(value = "file1", required = false) MultipartFile file1,
            @RequestPart(value = "file2", required = false) MultipartFile file2,
            @RequestPart(value = "file3", required = false) MultipartFile file3
    ) {
        List<MultipartFile> fileList = Arrays.stream((new MultipartFile[] {file1, file2, file3}))
                .filter(item -> item != null && !item.isEmpty()).toList();
        return ResponseEntity.status(CREATED).body(SuccessResponse.of(
                ResponseMessage.CLASS_REGISTER_SUCCESS,
                oneDayClassService.registerClass(userService.getCurrentUserEmail(), request, fileList))
        );
    }

    /**
     * Class 세부 정보 수정
     * @param   request
     * @return  ResponseEntity<SuccessResponse<ClassUpdateDto.ClassResponse>>
     */
    @Operation(summary = "Class 세부 정보 수정", description = "Class 세부 정보 수정")
    @PutMapping(path = "/class/{classId}")
    public ResponseEntity<SuccessResponse<ClassUpdateDto.ClassResponse>> updateClass(
            @PathVariable String classId,
            @RequestBody @Valid ClassUpdateDto.ClassRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.of(
                ResponseMessage.CLASS_UPDATE_SUCCESS,
                oneDayClassService.updateClass(userService.getCurrentUserEmail(), request, Long.parseLong(classId)))
        );
    }

    /**
     * Class 삭제
     * @param   classId
     * @return  ResponseEntity<SuccessResponse<ClassDto>>
     */
    @Operation(summary = "Class 삭제", description = "Class 삭제")
    @DeleteMapping(path = "/class/{classId}")
    public ResponseEntity<SuccessResponse<Boolean>> deleteClass(
            @PathVariable String classId
    ) {
        return ResponseEntity.status(OK).body(SuccessResponse.of(
                ResponseMessage.CLASS_DELETE_SUCCESS,
                oneDayClassService.deleteClass(userService.getCurrentUserEmail(), Long.parseLong(classId)))
        );
    }

    /**
     * Class FAQ 추가
     * @param   request
     * @return  ResponseEntity<SuccessResponse<ClassDto>>
     */
    @Operation(summary = "Class FAQ 추가", description = "Class FAQ 추가")
    @PostMapping(path = "/class/{classId}/faq")
    public ResponseEntity<SuccessResponse<ClassFAQDto>> registerFAQ(
            @RequestBody @Valid ClassFAQDto request,
            @PathVariable String classId
    ) {
        return ResponseEntity.status(CREATED).body(SuccessResponse.of(
                ResponseMessage.CLASS_FAQ_REGISTER_SUCCESS,
                oneDayClassService.registerFAQ(userService.getCurrentUserEmail(), request, Long.parseLong(classId)))
        );
    }

    /**
     * Class FAQ 수정
     * @param   request, classId, faqId
     * @return  ResponseEntity<SuccessResponse<ClassFAQDto>>
     */
    @Operation(summary = "Class FAQ 수정", description = "Class FAQ 수정")
    @PutMapping(path = "/class/{classId}/faq/{faqId}")
    public ResponseEntity<SuccessResponse<ClassFAQDto>> updateFAQ(
            @RequestBody @Valid ClassFAQDto request,
            @PathVariable String classId,
            @PathVariable String faqId
    ) {
        return ResponseEntity.status(OK).body(SuccessResponse.of(
                ResponseMessage.CLASS_FAQ_UPDATE_SUCCESS,
                oneDayClassService.updateFAQ(userService.getCurrentUserEmail(), request, Long.parseLong(classId), Long.parseLong(faqId)))
        );
    }

    /**
     * Class FAQ 삭제
     * @param   classId, faqId
     * @return  ResponseEntity<SuccessResponse<ClassFAQDto>>
     */
    @Operation(summary = "Class FAQ 삭제", description = "Class FAQ 삭제")
    @DeleteMapping(path = "/class/{classId}/faq/{faqId}")
    public ResponseEntity<SuccessResponse<Boolean>> deleteFAQ(
            @PathVariable String classId,
            @PathVariable String faqId
    ) {
        return ResponseEntity.status(OK).body(SuccessResponse.of(
                ResponseMessage.CLASS_FAQ_DELETE_SUCCESS,
                oneDayClassService.deleteFAQ(userService.getCurrentUserEmail(), Long.parseLong(classId), Long.parseLong(faqId)))
        );
    }

}
