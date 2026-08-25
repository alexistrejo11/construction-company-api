package io.github.alexisTrejo11.construction.company.config.security;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserStatus;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final UserRepository users;
  private final SecurityExceptionHandlers.CustomAccessDeniedHandler accessDeniedHandler;
  private final SecurityExceptionHandlers.CustomAuthenticationEntryPoint authenticationEntryPoint;
  @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
  @Bean AuthenticationManager authenticationManager() {
    var provider = new DaoAuthenticationProvider();
    provider.setPasswordEncoder(passwordEncoder());
    provider.setUserDetailsService(email -> users.findByEmail(email).filter(user -> user.getStatus() == UserStatus.ACTIVE)
        .map(user -> new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPasswordHash(), user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).toList()))
        .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials")));
    return provider::authenticate;
  }
  @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    var csrf = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrf.setCookiePath("/");
    var csrfRequestHandler = new CsrfTokenRequestAttributeHandler();

     return http.csrf(config -> config
             .csrfTokenRepository(csrf)
             .csrfTokenRequestHandler(csrfRequestHandler))
          .authorizeHttpRequests(auth -> auth
              .requestMatchers(
                  "/actuator/**",
                  "/v2/api/auth/csrf",
                  "/v2/api/auth/login",
                  "/v2/api/invitations/*/accept",
                  "/swagger-ui/**",
                  "/api-docs/**"
              ).permitAll()
              .anyRequest().authenticated())
         .securityContext(context -> context
             .securityContextRepository(new HttpSessionSecurityContextRepository())
             .requireExplicitSave(false))
         .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
         .exceptionHandling(exceptions -> exceptions
             .accessDeniedHandler(accessDeniedHandler)
             .authenticationEntryPoint(authenticationEntryPoint))
         .build();
  }

  @Bean
  DefaultCookieSerializer cookieSerializer(
      @Value("${app.security.session-cookie.secure:false}") boolean secureCookie
  ) {
    var serializer = new DefaultCookieSerializer();
    serializer.setCookieName("SESSION");
    serializer.setCookiePath("/");
    serializer.setUseHttpOnlyCookie(true);
    serializer.setSameSite("Lax");
    serializer.setUseSecureCookie(secureCookie);
    serializer.setCookieMaxAge(24 * 60 * 60);
    return serializer;
  }
}
