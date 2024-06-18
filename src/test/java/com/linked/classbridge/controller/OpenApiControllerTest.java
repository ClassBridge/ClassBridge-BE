package com.linked.classbridge.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.OpenApiService;
import com.linked.classbridge.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(OpenApiController.class)
@AutoConfigureMockMvc
public class OpenApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenApiService openApiService;

    @Test
    @DisplayName("사업자등록번호 확인 성공")
    @WithMockUser(roles = "USER")
    public void validate_business_registration_number_success() throws Exception {
        String businessRegistrationNumber = "1234567890";
        when(openApiService.validate(businessRegistrationNumber)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/openapi/validate/business-registration-number")
                        .param("businessRegistrationNumber", businessRegistrationNumber)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사업자등록번호 확인 실패 - 사업자등록번호가 10자리 숫자 문자열이 아닌 경우")
    @WithMockUser(roles = "USER")
    public void validate_failure_business_registration_number_not_valid() throws Exception {
        String businessRegistrationNumber = "12345";

        doThrow(new RestApiException(ErrorCode.NOT_VALID_BUSINESS_REGISTRATION_NUMBER))
                .when(openApiService).validate(businessRegistrationNumber);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/openapi/validate/business-registration-number")
                        .param("businessRegistrationNumber", businessRegistrationNumber)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
