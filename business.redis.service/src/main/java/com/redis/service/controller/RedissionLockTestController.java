package com.redis.service.controller;

import com.redis.service.lock.redissionlock.RedLockService;
import com.redis.service.model.ResultVO;
import com.redis.service.redlock.RedisLock;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * The type Redission lock test controller.
 */
@RequestMapping("v1/redlock")
@RestController
public class RedissionLockTestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedissionLockTestController.class);

    private final RedLockService redLockService;

    private static final String accountOrderLockKey = "ACCOUNT_PAY_UNIFIEDORDER_LOCK_KEY_TRADE_NO";

    private int count = 100;

    /**
     * Instantiates a new Redission lock test controller.
     * @param redLockService the red lock service
     */
    public RedissionLockTestController(RedLockService redLockService) {
        this.redLockService = redLockService;
    }

    /**
     * Take order result vo.
     * @return the result vo
     */
    @ApiOperation(value = "统一入口", notes = "统一入口")
    @RequestMapping(value = "order")
    public ResultVO takeOrder() {
        String lockKey = accountOrderLockKey + UUID.randomUUID().toString();
        boolean isLock = false;
        try {
            // 获得锁 注意锁的力度,只需要锁定需要防止并发的业务,锁的力度越低性能越好!
            isLock = redLockService.tryLockTimeout(lockKey, 5000, 10000, TimeUnit.MILLISECONDS);
            // 超时未获得锁
            if (!isLock) {
                return ResultVO.fail("操作太频繁,请稍后重试");
            }
            // 获取锁之后
            if (count <= 0) {
                redLockService.unLock(lockKey);
                LOGGER.info("抢完了,库存剩余为：{}", count);
                return ResultVO.fail("抢完了,抢购失败");
            }
            // 执行业务(需要锁定的部分)
            count--;
            LOGGER.info("抢购成功, 库存剩余为：{}", count);
            return ResultVO.success("抢购成功");
        } catch (Exception e) {
            LOGGER.error("TotalController.unifiedorder.error:", e);
            return ResultVO.fail("抢购失败");
        } finally {
            // 解锁
            if (isLock) {
                redLockService.unLock(lockKey);
            }
        }
    }

    @ApiOperation(value = "切面分布式RedLock锁", notes = "切面分布式RedLock锁")
    @RedisLock(prefix = "order", lockKey = "orderLock", waitTime = 3000, leaseTime = 6000, timeUnit = TimeUnit.MILLISECONDS)
    @RequestMapping(value = "order/test")
    public ResultVO takeOrderTest() {
        // 执行业务(需要锁定的部分)
        count--;
        LOGGER.info("抢购成功, 库存剩余为：{}", count);
        return ResultVO.success("抢购成功");
    }
}
