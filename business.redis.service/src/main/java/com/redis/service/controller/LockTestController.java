package com.redis.service.controller;

import com.redis.service.lock.stringLock.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Student controller.
 */
@RequestMapping("v1/string")
@RestController
@Slf4j
public class LockTestController {

    private final RedisLock redisLock;

    /**
     * Instantiates a new Lock test controller.
     * @param redisLock the redis lock
     */
    public LockTestController(RedisLock redisLock) {
        this.redisLock = redisLock;
    }

    private static final int TIMEOUT = 10 * 1000; //超时时间 10s

    private static final String ORDER_LOCK = "order_lock"; //超时时间 10s

    //库存
    //private AtomicInteger count = new AtomicInteger(100);

    private int count = 100;

    //抢购成功数
    private int success = 0;

    /**
     * Test lock string.
     * @return the string
     */
    @GetMapping("/testLock")
    public String testLock() {
        long time = System.currentTimeMillis() + TIMEOUT;
        //加锁
        boolean islock = redisLock.lock(ORDER_LOCK, String.valueOf(time));
        //没有获取到锁
        if (!islock) {
            log.info("服务器忙，请重试");
            return "服务器忙，请重试";

        }
        //获取锁之后
        if (count == 0) {
            redisLock.unlock(ORDER_LOCK, String.valueOf(time));
            log.info("抢完了,成功数为：{}", success);
            return "抢完了";
        }
        //因为获取到的锁的线程只有一个，所以这里计数也不会出错
        //库存减去1
        count--;
        /*  if (count.get()==0){
            redisLock.unlock("key",String.valueOf(time));
            log.info("抢完了,成功数为：{}",success);
            return "抢完了";
        }
        count.getAndDecrement();*/

        //解锁
        redisLock.unlock(ORDER_LOCK, String.valueOf(time));
        success++;
        log.info("抢购成功 成功数为：{} 库存剩余为：{}", success, count);
        return "抢购成功";

    }
}