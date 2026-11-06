package org.example.hotelbookingsyetem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    /** Injected everywhere "now" is needed, so tests can control time. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
