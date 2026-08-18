package com.devtrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DevTrack AI backend entrypoint.
 *
 * <p>See /docs/07_Backend_Architecture.md for the module/layering conventions every package under
 * com.devtrack follows, and /docs/04_System_Architecture.md for the overall system design this
 * application implements.
 */
@SpringBootApplication
public class DevTrackApplication {

  public static void main(String[] args) {
    SpringApplication.run(DevTrackApplication.class, args);
  }
}
