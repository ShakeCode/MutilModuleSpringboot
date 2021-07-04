package com.redis.service.aspect;

import com.redis.service.annotation.RedisLock;
import com.redis.service.lock.redissionlock.RedLockService;
import com.redis.service.model.ResultVO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 锁名称建议参考阿里redis开发规范:
 * https://developer.aliyun.com/article/531067
 * <p>
 * The type Redis lock aspect.
 */
@Aspect
@Component
public class RedisLockAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLockAspect.class);

    private final RedLockService redLockService;

    /**
     * Instantiates a new Redis lock aspect.
     * @param redLockService the red lock service
     */
    public RedisLockAspect(RedLockService redLockService) {
        this.redLockService = redLockService;
    }

    /**
     * Pointcut.
     */
    @Pointcut("@annotation(com.redis.service.annotation.RedisLock)")
    public void pointcut() {
    }

    /**
     * Do around object.
     * @param pjp the pjp
     * @return the object
     * @throws Throwable the throwable
     */
    @Around("pointcut()")
    public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().getName();
        Class<?> targetClass = pjp.getTarget().getClass();
        Class<?>[] paraTypes = ((MethodSignature) pjp.getSignature()).getParameterTypes();
        Method method = targetClass.getMethod(methodName, paraTypes);
        Object[] arguments = pjp.getArgs();
        LOGGER.info("分布式锁拦截,方法:{},参数:{}", methodName, arguments);
        Object[] args = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] instanceof ServletRequest || arguments[i] instanceof ServletResponse || arguments[i] instanceof MultipartFile) {
                continue;
            }
            args[i] = arguments[i];
        }
        RedisLock annotation = method.getAnnotation(RedisLock.class);
        String prefix = annotation.prefix();
        String key = annotation.lockKey();
        long leaseTime = annotation.leaseTime();
        Object result;
        // String lockParamKey = StringUtils.arrayToDelimitedString(args, ".");
        String lockParamKey = UUID.randomUUID().toString();
        String lockKey = this.getLockKey(prefix, key, targetClass.getSimpleName(), method.getName(), lockParamKey);
        if (redLockService.tryLockTimeout(lockKey, annotation.waitTime(), leaseTime, annotation.timeUnit())) {
            LOGGER.info("加锁成功:{}", lockKey);
            try {
                result = pjp.proceed();
            } finally {
                redLockService.lock(lockKey, leaseTime, annotation.timeUnit());
                LOGGER.info("释放分布式锁[{}]", lockKey);
            }
        } else {
            LOGGER.info("获取锁失败方法:{},参数:{}", methodName, arguments);
            return ResultVO.fail("获取锁失败");
        }
        return result;
    }

    private String getLockKey(String prefix, String key, String className, String invokedMethod, String lockParamKey) {
        return StringUtils.hasText(prefix) ? "lock".concat(":").concat(prefix).concat(":") + key.concat(":" + lockParamKey) : "lock".concat(":").concat(className).concat(":").concat(invokedMethod).concat(":" + lockParamKey);
    }
}
