package com.task.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.validation.constraints.NotNull;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The type Thread pool config.
 */
@Configuration
public class ThreadPoolConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolConfig.class);

    /**
     * 异步调度任务线程池配置
     * Thread pool task scheduler thread pool task scheduler.
     * @return the thread pool task scheduler
     */
    @Bean("asyncPromotionDelay")
    public ThreadPoolTaskScheduler scheduledThreadPoolExecutor() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setThreadNamePrefix("asyncPromotionDelay-");
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.setPoolSize(5);
        taskScheduler.setAwaitTerminationSeconds(60);
        taskScheduler.setErrorHandler(ex -> LOGGER.info("asyncPromotionDelay error:{}", ex.getMessage()));
        // 线程池对拒绝任务的处理策略
        taskScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return taskScheduler;
    }

    /**
     * 异步线程池配置
     * Async executor executor.
     * @return the executor
     */
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

    /**
     * The type Context decorator.
     */
    static class ContextDecorator implements TaskDecorator {
        /**
         * Decorate the given {@code Runnable}, returning a potentially wrapped
         * {@code Runnable} for actual execution.
         * @param runnable the original {@code Runnable}
         * @return the decorated {@code Runnable}
         */
        @Override
        public Runnable decorate(@NotNull Runnable runnable) {
            // 获取主线程中的请求信息（我们的用户信息也放在里面）
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return () -> {
                try {
                    // 将主线程的请求信息，设置到子线程中
                    RequestContextHolder.setRequestAttributes(attributes);
                    // 执行子线程，这一步不要忘了
                    runnable.run();
                } finally {
                    // 线程结束，清空这些信息，否则可能造成内存泄漏
                    RequestContextHolder.resetRequestAttributes();
                }
            };
        }
    }
}
