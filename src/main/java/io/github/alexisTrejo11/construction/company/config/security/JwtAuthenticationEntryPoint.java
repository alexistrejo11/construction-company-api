package io.github.alexisTrejo11.construction.company.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;
  public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }
  @Override public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), ResponseWrapper.failure("Authentication is required", Result.ErrorType.UNAUTHENTICATED, "UNAUTHENTICATED", null));
  }
}
