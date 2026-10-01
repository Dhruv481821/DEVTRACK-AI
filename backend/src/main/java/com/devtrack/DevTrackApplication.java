package com.devtrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * DevTrack AI backend entrypoint.
 *
 * <p>See /docs/07_Backend_Architecture.md for the module/layering conventions every package under
 * com.devtrack follows, and /docs/04_System_Architecture.md for the overall system design this
 * application implements.
 *
 * <p>{@code @EnableScheduling} added for GithubSyncScheduler (FR-GH-02) — single-instance-safe per
 * 04_System_Architecture.md §8; no other {@code @Scheduled} job exists yet.
 */
@SpringBootApplication
@EnableScheduling
public class DevTrackApplication {

  public static void main(String[] args) {
    SpringApplication.run(DevTrackApplication.class, args);
  }
}
