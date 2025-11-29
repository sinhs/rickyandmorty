package com.sid.rickmorty.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Separates scheduling toggle in case we need to disable it for tests or local runs.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
