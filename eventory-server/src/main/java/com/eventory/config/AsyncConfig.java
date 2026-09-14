package com.eventory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** @Async 활성화 — QR 메일처럼 응답을 기다릴 필요 없는 작업을 별도 스레드(applicationTaskExecutor)에서 실행 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
