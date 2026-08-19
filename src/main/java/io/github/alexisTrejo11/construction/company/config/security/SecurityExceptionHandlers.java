package io.github.alexisTrejo11.construction.company.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class SecurityExceptionHandlers {
  @Component
  public static class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;
    public CustomAccessDeniedHandler(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }
    @Override public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      objectMapper.writeValue(response.getOutputStream(), ResponseWrapper.failure("Access is denied", Result.ErrorType.FORBIDDEN, "ACCESS_DENIED", null));
    }
  }
}
