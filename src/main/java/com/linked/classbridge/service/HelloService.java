package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.HELLO_NAME_IS_REQUIRED;
import static com.linked.classbridge.type.ErrorCode.HELLO_NOT_FOUND;

import com.linked.classbridge.domain.Hello;
import com.linked.classbridge.dto.HelloDto.HelloRequest;
import com.linked.classbridge.dto.HelloDto.HelloResponse;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.HelloRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HelloService {

    private final HelloRepository helloRepository;


    /**
     * Hello 등록
     *
     * @param request Hello 등록 요청 dto
     * @return Hello 응답 dto
     */
    @Transactional
    public HelloResponse registerHello(HelloRequest request) {
        validateHello(request);
        Hello savedHello = helloRepository.save(HelloRequest.toEntity(request));
        return HelloResponse.fromEntity(savedHello);
    }

    /**
     * Hello 조회
     *
     * @param id Hello id
     * @return Hello 응답 dto
     */
    public HelloResponse getHello(Long id) {
        return HelloResponse.fromEntity(
                helloRepository.findById(id)
                        .orElseThrow(() -> new RestApiException(HELLO_NOT_FOUND)));
    }

    /**
     * Hello 수정
     *
     * @param id      Hello id
     * @param request Hello 수정 요청 dto
     * @return Hello 응답 dto
     */
    @Transactional
    public HelloResponse updateHello(Long id, HelloRequest request) {
        validateHello(request);
        Hello hello = helloRepository.findById(id)
                .orElseThrow(() -> new RestApiException(HELLO_NOT_FOUND));
        hello.update(request);
        return HelloResponse.fromEntity(hello);
    }

    /**
     * HelloRequest 유효성 검증
     *
     * @param request HelloRequest
     */
    public void validateHello(HelloRequest request) {
        if (request.name() == null || request.name().isEmpty()) {
            log.error("Hello name is required. : {}", request);
            throw new RestApiException(HELLO_NAME_IS_REQUIRED);
        }
    }
}
