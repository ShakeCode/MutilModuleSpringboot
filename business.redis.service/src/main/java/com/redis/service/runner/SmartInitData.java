package com.redis.service.runner;

import com.redis.service.annotation.LogTime;
import com.redis.service.annotation.RedisLock;
import com.redis.service.service.CacheStudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Configuration;

/**
 * The type Smart init data. 初始化bean后置处理,处理完后才打开Tomcat端口
 */
@Configuration
public class SmartInitData implements SmartInitializingSingleton {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmartInitData.class);

    private final ListableBeanFactory listableBeanFactory;

    /**
     * Instantiates a new Smart init data.
     * @param listableBeanFactory the listable bean factory
     */
    public SmartInitData(ListableBeanFactory listableBeanFactory) {
        this.listableBeanFactory = listableBeanFactory;
    }

    /**
     * Invoked right at the end of the singleton pre-instantiation phase,
     * with a guarantee that all regular singleton beans have been created
     * already. {@link ListableBeanFactory#getBeansOfType} calls within
     * this method won't trigger accidental side effects during bootstrap.
     * <p><b>NOTE:</b> This callback won't be triggered for singleton beans
     * lazily initialized on demand after {@link BeanFactory} bootstrap,
     * and not for any other bean scope either. Carefully use it for beans
     * with the intended bootstrap semantics only.
     */
    @LogTime(operationMessage = "init cache")
    @RedisLock(prefix = "init data", lockKey = "initDataLock", waitTime = 3000, leaseTime = 6000)
    @Override
    public void afterSingletonsInstantiated() {
        LOGGER.info("SmartInitData...");
        CacheStudentService studentService = listableBeanFactory.getBean(CacheStudentService.class);
        LOGGER.info("queryStudentByCode:{}", studentService.queryStudentByCode("1cc", "1cc"));
    }
}
