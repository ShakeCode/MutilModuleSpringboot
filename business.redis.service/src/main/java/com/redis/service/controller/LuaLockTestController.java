package com.redis.service.controller;

import com.redis.service.lock.lua.LuaRedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The type Student controller.
 */
@RequestMapping("v1/lua")
@RestController
@Slf4j
public class LuaLockTestController {
    private final LuaRedisLock luaRedisLock;

    private static final String ORDER_KEY = "order";

    private AtomicInteger ORDER_COUNT = new AtomicInteger(100);

    /**
     * Instantiates a new Lua lock test controller.
     * @param luaRedisLock the lua redis lock
     */
    public LuaLockTestController(LuaRedisLock luaRedisLock) {
        this.luaRedisLock = luaRedisLock;
    }

    /**
     * Test lock string.
     * @return the string
     * @throws InterruptedException the interrupted exception
     */
    @GetMapping("/thread/lock")
    public String testMutilThreadLock() throws InterruptedException {
        ArrayList<Thread> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(() -> {
                if (luaRedisLock.lock(ORDER_KEY, 2000, 3)) {
                    try {
                        // 成功获取锁
                        log.info("{} 获取锁成功,继续执行任务", Thread.currentThread().getName());
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        log.error("excepiotn ", e);
                    } finally {
                        luaRedisLock.unlock(ORDER_KEY);
                    }
                }
            });
            list.add(t);
            t.start();
        }
        for (Thread t : list) {
            t.join();
        }
        Thread.sleep(2000);
        return "success";
    }

    @GetMapping("/lock")
    public String testLock() {
        boolean isLock = luaRedisLock.lock(ORDER_KEY, 3000, 3);
        // 没有获取到锁
        if (!isLock) {
            log.info("服务器忙，请重试");
            return "服务器忙，请重试";
        }
        // 获取锁之后
        if (ORDER_COUNT.get() <= 0) {
            luaRedisLock.unlock(ORDER_KEY);
            log.info("抢完了,库存剩余:{}", ORDER_COUNT.get());
            return "抢完了";
        }
        try {
            // 成功获取锁
            log.info("{} 获取锁成功,库存剩余:{},继续执行任务", Thread.currentThread().getName(), ORDER_COUNT.getAndDecrement());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            log.error("lock excepiotn", e);
        } finally {
            luaRedisLock.unlock(ORDER_KEY);
        }
        return "success";
    }
}