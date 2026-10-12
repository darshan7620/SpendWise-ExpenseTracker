package com.spendwise.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String body = "{"
                + "\"timestamp\":\"" + LocalDateTime.now() + "\","
                + "\"status\":403,"
                + "\"errorCode\":\"FORBIDDEN\","
                + "\"message\":\"You do not have permission to access this resource\","
                + "\"fieldErrors\":null"
                + "}";
        response.getWriter().write(body);
    }
}
