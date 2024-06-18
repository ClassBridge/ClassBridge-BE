package com.linked.classbridge.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.linked.classbridge.service.JWTService;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(ReissueController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReissueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JWTService jwtService;

    @Test
    public void reissue_success() throws Exception {

        when(jwtService.isExpired("validRefreshToken")).thenReturn(false);
        when(jwtService.getTokenType("validRefreshToken")).thenReturn("refresh");
        when(jwtService.getEmail("validRefreshToken")).thenReturn("test@example.com");
        when(jwtService.getRoles("validRefreshToken")).thenReturn(Arrays.asList("ROLE_USER"));
        when(jwtService.createJwt("access", "test@example.com", Arrays.asList("ROLE_USER"), 600000L)).thenReturn("newAccessToken");
        when(jwtService.createJwt("refresh", "test@example.com", Arrays.asList("ROLE_USER"), 86400000L)).thenReturn("newRefreshToken");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("refresh", "validRefreshToken")))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void reissue_fail_no_refresh_token() throws Exception {

        // refresh 토큰이 없는 경우
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("refresh", null)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void reissue_fail_expired_refresh_token() throws Exception {
        // refresh 토큰이 만료된 경우
        when(jwtService.isExpired("expiredRefreshToken")).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("refresh", "expiredRefreshToken")))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void reissue_fail_invalid_refresh_token() throws Exception {

        when(jwtService.isExpired("invalidRefreshToken")).thenReturn(false);
        // refresh 토큰이 아닌 access 토큰이 있는 경우
        when(jwtService.getTokenType("invalidRefreshToken")).thenReturn("access");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("refresh", "invalidRefreshToken")))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
