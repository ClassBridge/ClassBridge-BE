package com.linked.classbridge.controller;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.oneDayClass.ClassDto.ClassResponseByUser;
import com.linked.classbridge.dto.oneDayClass.ClassSearchDto;
import com.linked.classbridge.dto.review.GetReviewResponse;
import com.linked.classbridge.service.OneDayClassService;
import com.linked.classbridge.service.ReviewService;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.CategoryType;
import com.linked.classbridge.type.LocationType;
import com.linked.classbridge.type.OrderType;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/class")
public class OneDayClassController {

    private final ReviewService reviewService;
    private final OneDayClassService oneDayClassService;
    private final UserService userService;

    @Operation(summary = "클래스 리뷰 조회", description = "클래스 리뷰 조회")
    @GetMapping("/{classId}/reviews")
    public ResponseEntity<SuccessResponse<Page<GetReviewResponse>>> getClassReviews(
            @PathVariable Long classId,
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.REVIEW_GET_SUCCESS,
                        reviewService.getClassReviews(classId, pageable)
                )
        );
    }

    @Operation(summary = "클래스 상세 조회", description = "클래스 상세 조회")
    @GetMapping("/{classId}")
    public ResponseEntity<SuccessResponse<ClassResponseByUser>> getClassDetail(
            @PathVariable Long classId
    ) {
        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.ONE_DAY_CLASS_GET_SUCCESS,
                        oneDayClassService.getOneDayClassByUser(userService.checkLogin() ? userService.getCurrentUserEmail() : null, classId)
                )
        );
    }

    @Operation(summary = "클래스 검색", description = "클래스 검색")
    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<Page<ClassSearchDto>>> searchClass
             (@RequestParam(name = "query", required = false, defaultValue = "") String query,
              @RequestParam(name = "category", required = false, defaultValue = "") CategoryType categoryType,
              @RequestParam(name = "lat", required = false, defaultValue = "0.0") double lat,
              @RequestParam(name = "lng", required = false, defaultValue = "0.0") double lng,
              @RequestParam(name = "location", required = false, defaultValue = "") LocationType location,
              @RequestParam(name = "order", required = false, defaultValue = "WISH") OrderType orderType,
              @RequestParam(name = "page", required = false, defaultValue = "1") int page) {
        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.ONE_DAY_CLASS_SEARCH_SUCCESS,
                        oneDayClassService.searchClass(userService.checkLogin() ? userService.getCurrentUserEmail() : null,
                                query, categoryType, lat, lng, location, orderType, page)
                )
        );
    }

    @Operation(summary = "클래스 자동 완성", description = "클래스 자동 완성")
    @GetMapping("/autoComplete")
    public ResponseEntity<SuccessResponse<List<String>>> autoCompleteSearch
            (@RequestParam(name = "query", required = false, defaultValue = "") String query) throws IOException {
        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.ONE_DAY_CLASS_AUTO_COMPLETE_SUCCESS,
                        oneDayClassService.autoCompleteSearch(query)
                )
        );
    }
}
