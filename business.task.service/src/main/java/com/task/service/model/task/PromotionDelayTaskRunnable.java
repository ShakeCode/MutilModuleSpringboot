package com.task.service.model.task;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Promotion delay task runnable.
 */
@Data
public class PromotionDelayTaskRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionDelayTaskRunnable.class);

    private String name;

    /**
     * Instantiates a new Promotion delay task runnable.
     * @param name the name
     */
    public PromotionDelayTaskRunnable(String name) {
        this.name = name;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     * @see Thread#run()
     */
    @Override
    public void run() {
        LOGGER.info("exec async task scheduler,name:{}", this.name);
    }
}
