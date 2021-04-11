package com.search.service.service.impl;

import com.search.service.service.AsyncSmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Service
public class AsyncSmsServiceImpl implements AsyncSmsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncSmsServiceImpl.class);

    @Async("asyncExecutor")
    @Override
    public String getName() {
        // 此处由于有线程装饰器传递上下文得以获取上下文
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        LOGGER.info("当前线程为:{},请求方法为:{},请求路径为:{}", Thread.currentThread().getName(), request.getMethod(), request.getRequestURL().toString());
        return "xiaoming";
    }
}
