package com.task.service.service;

import com.task.service.model.task.PromotionDelayTaskRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * The type Dynamic task service.
 */
@Service
public class DynamicTaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicTaskService.class);

    private final ConcurrentHashMap<String, ScheduledFuture<?>> SCHEDULED_CONCURRENT_MAP = new ConcurrentHashMap<>();

    private final ThreadPoolTaskScheduler taskScheduler;

    /**
     * Instantiates a new Dynamic task service.
     * @param taskScheduler the task scheduler
     */
    public DynamicTaskService(@Qualifier("asyncPromotionDelay") ThreadPoolTaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }


    /**
     * Add task boolean.
     * @param name the name
     * @param cron the cron
     * @return the boolean
     */
    public boolean addTask(String name, String cron) {
        if (SCHEDULED_CONCURRENT_MAP.contains(name) && SCHEDULED_CONCURRENT_MAP.get(name) != null) {
            return false;
        }
        ScheduledFuture<?> schedule = taskScheduler.schedule(new PromotionDelayTaskRunnable(name), new CronTrigger(cron));
        SCHEDULED_CONCURRENT_MAP.put(name, schedule);
        LOGGER.info("add async task scheduler success");
        return true;
    }

    /**
     * Delete task boolean.
     * @param name the name
     * @return the boolean
     */
    public boolean deleteTask(String name) {
        if (!SCHEDULED_CONCURRENT_MAP.containsKey(name) || SCHEDULED_CONCURRENT_MAP.get(name) == null) {
            return false;
        }
        ScheduledFuture<?> schedule = SCHEDULED_CONCURRENT_MAP.get(name);
        schedule.cancel(true);
        LOGGER.info("cancle async task scheduler success");
        return true;
    }
}
