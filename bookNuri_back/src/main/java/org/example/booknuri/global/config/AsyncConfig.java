package org.example.booknuri.global.config;

//병렬 처리를 가능하게 해주는 설정

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


@Configuration
//@Async 붙인 메서드들이 실제로 비동기로 동작하게 함
@EnableAsync  // @Async 기능을 사용할 수 있도록 활성화하는 어노테이션
public class AsyncConfig {

    // 실제 병렬 작업을 담당할 스레드 풀을 설정해주는 메서드
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(10);       // 동시에 10개 처리
        executor.setMaxPoolSize(17);        // 최대 17개까지 확장
        executor.setQueueCapacity(100);     // 대기열 100개
        executor.setThreadNamePrefix("Async-LogSaver-");

        //  만약 큐도 꽉 찼고 스레드도 꽉 찼다면?
        //  이때 그냥 에러내는 게 아니라, "요청한 스레드가 직접 실행"하게 함
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
