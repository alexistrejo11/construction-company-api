package io.github.alexisTrejo11.construction.company.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TraceIdFilter extends OncePerRequestFilter {
  public static final String HEADER = "X-Trace-Id";
  public static final String MDC_KEY = "traceId";
  @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    var traceId = UUID.randomUUID().toString();
    MDC.put(MDC_KEY, traceId);
    response.setHeader(HEADER, traceId);
    try { chain.doFilter(request, response); } finally { MDC.remove(MDC_KEY); }
  }
}
