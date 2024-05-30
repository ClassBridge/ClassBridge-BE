package com.linked.classbridge.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.linked.classbridge.dto.HelloDto.HelloRequest;
import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.service.HelloService;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hello")
public class HelloController {

    private final HelloService helloService;

    @Operation(summary = "Hello 조회", description = "Hello 조회")
    @GetMapping("/{helloId}")
    public ResponseEntity<SuccessResponse<?>> getHello(
            @PathVariable Long helloId
    ) {
        return ResponseEntity.status(CREATED).body(SuccessResponse.of(
                ResponseMessage.HELLO_GET_SUCCESS,
                helloService.getHello(helloId))
        );
    }

    @Operation(summary = "Hello 등록", description = "Hello 등록")
    @PostMapping
    public ResponseEntity<SuccessResponse<?>> registerHello(
            @RequestBody @Valid HelloRequest request
    ) {
        return ResponseEntity.status(CREATED).body(SuccessResponse.of(
                ResponseMessage.HELLO_REGISTER_SUCCESS,
                helloService.registerHello(request))
        );
    }

    @Operation(summary = "Hello 수정", description = "Hello 수정")
    @PutMapping("/{helloId}")
    public ResponseEntity<SuccessResponse<?>> updateHello(
            @PathVariable Long helloId,
            @ModelAttribute @Valid HelloRequest request
    ) {
        return ResponseEntity.status(CREATED).body(SuccessResponse.of(
                ResponseMessage.HELLO_UPDATE_SUCCESS,
                helloService.updateHello(helloId, request))
        );
    }

}
