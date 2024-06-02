package com.linked.classbridge.controller;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.review.DeleteReviewResponse;
import com.linked.classbridge.dto.review.GetReviewResponse;
import com.linked.classbridge.dto.review.RegisterReviewDto;
import com.linked.classbridge.dto.review.UpdateReviewDto;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.service.ReviewService;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final UserRepository userRepository;
    private final ReviewService reviewService;

    @Operation(summary = "리뷰 등록", description = "리뷰 등록")
    @PostMapping
    public ResponseEntity<SuccessResponse<RegisterReviewDto.Response>> registerReview(
            @Valid @ModelAttribute RegisterReviewDto.Request request
    ) {
        User user = userRepository.findById(1L).orElse(null);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.of(
                        ResponseMessage.REVIEW_REGISTER_SUCCESS,
                        reviewService.registerReview(user, request)
                )
        );
    }

    @Operation(summary = "리뷰 수정", description = "리뷰 수정")
    @PutMapping("/{reviewId}")
    public ResponseEntity<SuccessResponse<UpdateReviewDto.Response>> updateReview(
            @Valid @ModelAttribute UpdateReviewDto.Request request,
            @PathVariable Long reviewId
    ) {
        User user = userRepository.findById(1L).orElse(null);

        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.REVIEW_UPDATE_SUCCESS,
                        reviewService.updateReview(user, request, reviewId)
                )
        );
    }

    @Operation(summary = "리뷰 삭제", description = "리뷰 삭제")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<SuccessResponse<DeleteReviewResponse>> deleteReview(
            @PathVariable Long reviewId
    ) {
        User user = userRepository.findById(1L).orElse(null);

        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.REVIEW_DELETE_SUCCESS,
                        reviewService.deleteReview(user, reviewId)
                )
        );
    }

    @Operation(summary = "리뷰 조회", description = "리뷰 조회")
    @GetMapping("/{reviewId}")
    public ResponseEntity<SuccessResponse<GetReviewResponse>> getReview(
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.REVIEW_GET_SUCCESS,
                        reviewService.getReview(reviewId)
                )
        );
    }
}
