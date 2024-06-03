package com.linked.classbridge.controller;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.review.GetReviewResponse;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.service.ReviewService;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final ReviewService reviewService;

    private final UserRepository userRepository;

    @Operation(summary = "수강생 리뷰 조회", description = "수강생 리뷰 조회")
    @GetMapping("/reviews")
    public ResponseEntity<SuccessResponse<Page<GetReviewResponse>>> getClassReviews(
            @PageableDefault Pageable pageable
    ) {

        User user = userRepository.findById(1L).orElse(null);

        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.REVIEW_GET_SUCCESS,
                        reviewService.getUserReviews(user, pageable)
                )
        );
    }
}
