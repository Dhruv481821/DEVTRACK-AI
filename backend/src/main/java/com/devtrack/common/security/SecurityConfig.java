package com.devtrack.common.security;

import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * Implements /docs/07_Backend_Architecture.md §5 and /docs/12_Security.md §11: stateless sessions
 * (JWT per request, no server-side session state), strict CORS origin allowlist (never a wildcard),
 * no separate CSRF-token mechanism — the SameSite=Strict refresh cookie + Bearer-header auth for
 * everything else already makes this API CSRF-immune by construction, per 12_Security.md §11's
 * reasoning.
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
    http.csrf(
            AbstractHttpConfigurer
                ::disable) // see class docblock — not needed given the token transport design
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
                    // FR-AUTH-* — registration/login/refresh must be reachable unauthenticated
                    .requestMatchers("/api/v1/auth/**")
                    .permitAll()
                    // Spring's own OAuth2 authorization-request and callback endpoints —
                    // FR-AUTH-02's Google path, must be reachable unauthenticated by definition
                    .requestMatchers("/oauth2/**", "/login/oauth2/**")
                    .permitAll()
                    // Railway health check target — 14_DevOps.md §7
                    .requestMatchers("/actuator/health")
                    .permitAll()
                    // API docs — dev-profile only in practice (disabled in prod,
                    // application-prod.yml)
                    .requestMatchers("/api/v1/docs/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(oauth2 -> oauth2.successHandler(oauth2LoginSuccessHandler))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(corsProperties.allowedOrigin()));
    config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(true); // required so the refresh-token cookie is sent cross-origin

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
