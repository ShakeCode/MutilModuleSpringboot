package com.search.service.config;

import org.springframework.core.task.TaskDecorator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.validation.constraints.NotNull;

public class ContextDecorator implements TaskDecorator {
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