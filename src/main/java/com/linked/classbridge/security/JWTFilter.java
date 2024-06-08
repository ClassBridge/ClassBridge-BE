package com.linked.classbridge.security;

import com.linked.classbridge.dto.user.UserDto;
import com.linked.classbridge.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTService jwtService;

    public JWTFilter(JWTService jwtService) {

        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.info("Starting JWTFilter for request: {}", request.getRequestURI());

        String accessToken = request.getHeader("access");

        if (accessToken == null) {
            log.info("Access token is null, proceeding without authentication.");
            filterChain.doFilter(request, response);
            log.info("Completed JWTFilter for request: {}", request.getRequestURI());
            return;
        }

        if(jwtService.isExpired(accessToken)) {
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");

            log.info("Access token is expired, proceeding without authentication.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String category = jwtService.getCategory(accessToken);

        if (!category.equals("access")) {
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            log.info("Invalid access token, proceeding without authentication.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String email = jwtService.getEmail(accessToken);
        List<String> roles = jwtService.getRoles(accessToken);
        log.info("Token validated. UserEmail: {}, Roles: {}", email, roles);

        UserDto userDto = new UserDto();
        userDto.setEmail(email);
        userDto.setRoles(roles);

        CustomUserDetails customUserDetails = new CustomUserDetails(userDto);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.info("User authenticated and set in SecurityContext: {}", email);

        filterChain.doFilter(request, response);
        log.info("Completed JWTFilter for request: {}", request.getRequestURI());
    }
}
