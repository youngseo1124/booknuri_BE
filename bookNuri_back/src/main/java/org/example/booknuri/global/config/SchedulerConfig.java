package org.example.booknuri.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling // ✨ 이거 있어야 스케줄러들이 돌아간다!!
public class SchedulerConfig {
}
