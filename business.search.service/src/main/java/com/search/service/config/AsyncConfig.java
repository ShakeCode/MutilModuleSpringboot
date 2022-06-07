package com.search.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * The {@link Executor} instance to be used when processing async
     * method invocations.
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200); // 缓冲队列数
        executor.setKeepAliveSeconds(60); // 允许线程空闲时间（单位：默认为秒）
        executor.setThreadNamePrefix("async-A-");
        // 异步Task装饰器
        executor.setTaskDecorator(new ContextDecorator());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * 自定义异步线程异常处理器,适合处理没有返回值的异步线程
     * The {@link AsyncUncaughtExceptionHandler} instance to be used
     * when an exception is thrown during an asynchronous method execution
     * with {@code void} return type.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            /**
             * Handle the given uncaught exception thrown from an asynchronous method.
             * @param ex     the exception thrown from the asynchronous method
             * @param method the asynchronous method
             * @param params the parameters used to invoked the method
             */
            @Override
            public void handleUncaughtException(Throwable ex, Method method, Object... params) {
                LOGGER.error("Async execution error:{},method:{}", ex.getMessage(), method.toString());
            }
        };
    }
}
