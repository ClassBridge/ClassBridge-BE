package com.linked.classbridge.controller;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.OpenApiService;
import com.linked.classbridge.type.ErrorCode;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/openapi")
public class OpenApiController {

    private final OpenApiService openApiService;

    @Operation(summary = "사업자등록번호 확인", description = "사업자등록번호 진위여부 확인")
    @GetMapping("/validate/business-registration-number")
    public ResponseEntity<SuccessResponse<Boolean>> validate(
            @RequestParam String businessRegistrationNumber) throws URISyntaxException {

        if(businessRegistrationNumber == null || businessRegistrationNumber.length() != 10) {
            throw new RestApiException(ErrorCode.NOT_VALID_BUSINESS_REGISTRATION_NUMBER);
        }

        if(openApiService.validate(businessRegistrationNumber)) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse.of(
                            ResponseMessage.VALIDATE_BUSINESS_REGISTRATION_NUMBER_SUCCESS,
                            true
                    )
            );
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse.of(
                            ResponseMessage.NOT_VALID_BUSINESS_REGISTRATION_NUMBER,
                            false
                    )
            );
        }
    }
}
