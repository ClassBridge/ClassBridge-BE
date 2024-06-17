package com.linked.classbridge.controller;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.badge.BadgeResponse;
import com.linked.classbridge.service.BadgeService;
import com.linked.classbridge.service.JWTService;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/badges")
public class BadgeController {

    private final BadgeService badgeService;

    private final JWTService jwtService;

    @Operation(summary = "뱃지 추가", description = "(테스트 환경) 개발자가 뱃지를 직접 등록합니다.")
    @PostMapping("/add/{badgeName}")
    public ResponseEntity<SuccessResponse<MultipartFile>> uploadBadge(
            @PathVariable String badgeName,
            @RequestParam("badgeImage") MultipartFile badgeImage
    ) {

        badgeService.uploadBadge(badgeName, badgeImage);

        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.UPLOAD_BADGE_SUCCESS,
                        badgeImage
                )
        );
    }

    @Operation(summary = "뱃지 조회", description = "사용자의 뱃지 목록을 조회합니다.")
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<SuccessResponse<List<BadgeResponse>>> getBadges(HttpServletRequest request) {

        String userEmail = jwtService.getEmail(request.getHeader("access"));
        List<BadgeResponse> badgeList = badgeService.getBadges(userEmail);

        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.GET_USER_BADGES_SUCCESS,
                        badgeList
                )
        );
    }
}
