package com.linked.classbridge.controller;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.review.RegisterReviewDto;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.service.ReviewService;
import com.linked.classbridge.type.ResponseMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final UserRepository userRepository;
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<SuccessResponse<?>> registerReview(
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
}
