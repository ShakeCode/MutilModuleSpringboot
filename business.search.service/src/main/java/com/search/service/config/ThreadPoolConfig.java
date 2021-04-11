package com.search.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {

    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 核心线程数（默认线程数）
        executor.setMaxPoolSize(20); // 最大线程数
        executor.setQueueCapacity(200); // 缓冲队列数
        executor.setKeepAliveSeconds(60); // 允许线程空闲时间（单位：默认为秒）
        executor.setThreadNamePrefix("asyncExecutor-"); // 线程池名前缀
        executor.setTaskDecorator(new ContextDecorator());
        // 线程池对拒绝任务的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }
}
