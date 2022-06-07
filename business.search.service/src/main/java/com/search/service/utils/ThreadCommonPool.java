package com.search.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The type Thread common pool.
 */
@Slf4j
public class ThreadCommonPool {

    /**
     * The constant FIXED_THREAD_POOL.
     */
    public static final ExecutorService FIXED_THREAD_POOL = new ThreadPoolExecutor(5, 10,
            60, TimeUnit.SECONDS,
            new SynchronousQueue<>(true), new CustomizableThreadFactory("common-thread-"));

    /**
     * The type Named thread factory.自定义线程工厂设置线程前缀
     */
    static class NamedThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        /**
         * Instantiates a new Named thread factory.
         * @param name the name
         */
        NamedThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            if (null == name || name.isEmpty()) {
                name = "pool";
            }
            namePrefix = name + "-" + POOL_NUMBER.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    /**
     * The Executor one.
     */
    static ThreadPoolExecutor executorOne = new ThreadPoolExecutor(5, 5, 1, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(), new NamedThreadFactory("ASYN-ACCEPT-POOL"));

    /**
     * The Executor two.
     */
    static ThreadPoolExecutor executorTwo = new ThreadPoolExecutor(5, 5, 1, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(), new NamedThreadFactory("ASYN-PROCESS-POOL"));

    /**
     * The entry point of application.
     * @param args the input arguments
     */
    public static void main(String[] args) {

        FIXED_THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                log.info("nihao");
            }
        });  
        FIXED_THREAD_POOL.shutdown();

        //接受用户链接模块
        executorOne.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("接受用户链接线程");
                throw new NullPointerException();
            }
        });
        //具体处理用户请求模块
        executorTwo.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("具体处理业务请求线程");
            }
        });
        executorOne.shutdown();
        executorTwo.shutdown();

    }
}
