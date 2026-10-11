package com.devtrack.common.security;

import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Implements /docs/07_Backend_Architecture.md §5 and /docs/12_Security.md §11:
 * stateless sessions, strict CORS origin allowlist, and JWT authentication.
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JsonAuthEntryPoint authEntryPoint;
  private final JsonAccessDeniedHandler accessDeniedHandler;
  private final CorsProperties corsProperties;
  private final AuthenticationSuccessHandler oauth2LoginSuccessHandler;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthenticationFilter,
      JsonAuthEntryPoint authEntryPoint,
      JsonAccessDeniedHandler accessDeniedHandler,
      CorsProperties corsProperties,
      AuthenticationSuccessHandler oauth2LoginSuccessHandler) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.authEntryPoint = authEntryPoint;
    this.accessDeniedHandler = accessDeniedHandler;
    this.corsProperties = corsProperties;
    this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandler;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(authEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    // Authentication endpoints must be accessible without authentication.
                    .requestMatchers("/api/v1/auth/**")
                    .permitAll()
                    .requestMatchers("/api/v1/github/callback")
                    .permitAll()

                    // OAuth2 authorization and callback endpoints.
                    .requestMatchers("/oauth2/**", "/login/oauth2/**")
                    .permitAll()

                    // Health check.
                    .requestMatchers("/actuator/health")
                    .permitAll()

                    // API documentation.
                    .requestMatchers("/api/v1/docs/**")
                    .permitAll()

                    // Portfolio settings require an authenticated USER.
                    // Keep this rule before the public portfolio GET rule.
                    .requestMatchers("/api/v1/portfolio/me")
                    .hasRole("USER")

                    // Public portfolios are readable without authentication.
                    .requestMatchers(HttpMethod.GET, "/api/v1/portfolio/*")
                    .permitAll()

                    // All remaining endpoints require authentication.
                    .anyRequest()
                    .authenticated())
        .oauth2Login(oauth2 -> oauth2.successHandler(oauth2LoginSuccessHandler))
        .addFilterBefore(
            jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(corsProperties.allowedOrigin()));
    config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(true);
    config.setExposedHeaders(List.of("Retry-After"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
