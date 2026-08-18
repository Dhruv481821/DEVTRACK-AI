package com.devtrack.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Deliberately separate from SecurityConfig — see the circular-dependency this
 * fixed: AuthService needs PasswordEncoder → previously defined inside
 * SecurityConfig → which needs OAuth2LoginSuccessHandler → which needs AuthService
 * → the bean Spring was already constructing. Keeping this bean in its own
 * dependency-free class means AuthService never has to touch SecurityConfig (or
 * anything downstream of it) at all. Bcrypt, cost 12 — see /docs/12_Security.md §3.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
