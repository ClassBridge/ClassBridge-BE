package com.linked.classbridge.controller;

import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.service.RecommendationService;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.ErrorCode;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/class/recommend")
public class RecommendationController {

    private final UserService userService;

    private final RecommendationService recommendationService;

    private final OneDayClassRepository oneDayClassRepository;

    @Operation(summary = "추천 클래스 반환", description = "(비동기 처리) 사용자에게 맞는 추천 클래스를 반환")
    @GetMapping("/async")
    public CompletableFuture<ResponseEntity<SuccessResponse<List<OneDayClass>>>> recommendClassesAsync() {

        String userEmail = userService.getCurrentUserEmail();

        if(userEmail == null) {
            throw new RestApiException(ErrorCode.USER_NOT_FOUND);
        }

        return recommendationService.recommendClasses(userEmail).thenApply(result ->
                ResponseEntity.ok(
                        SuccessResponse.of(
                                ResponseMessage.GET_TOP_CLASSES_FOR_USER_SUCCESS,
                                result
                        )
                )
        );
    }

    @Operation(summary = "기본 추천 클래스 반환", description = "기본 추천 클래스를 반환")
    @GetMapping("/basic")
    public ResponseEntity<SuccessResponse<List<OneDayClass>>> recommendClassesBasic() {

        List<OneDayClass> topClasses = oneDayClassRepository.findTopClassesByRatingAndWish(PageRequest.of(0, 5));

        return ResponseEntity.ok(
                SuccessResponse.of(
                        ResponseMessage.GET_TOP_CLASSES_SUCCESS,
                        topClasses
                )
        );
    }
}
