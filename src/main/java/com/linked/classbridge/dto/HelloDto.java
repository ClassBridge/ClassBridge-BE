package com.linked.classbridge.dto;

import com.linked.classbridge.domain.Hello;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class HelloDto {

    public record HelloRequest(
            @Schema(description = "이름", example = "연결고리", minLength = 2, maxLength = 20)
            @NotBlank(message = "이름을 입력해 주세요.")
            @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하로 입력해 주세요.")
            String name,

            @Schema(description = "나이", example = "20", minimum = "0")
            @NotNull(message = "나이를 입력해 주세요.")
            Integer age
    ) {
        public static Hello toEntity(HelloRequest request) {
            return Hello.builder()
                    .name(request.name())
                    .age(request.age())
                    .build();
        }
    }

    public record HelloResponse(
            Long helloId,
            String name,
            int age
    ) {
        public static HelloResponse fromEntity(Hello hello) {
            return new HelloResponse(hello.getHelloId(), hello.getName(), hello.getAge());
        }
    }

}
