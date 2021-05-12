package com.task.service.utils;


import com.task.service.model.HeaderInfo;

/**
 * The type Thread header local util.
 */
public class ThreadHeaderLocalUtil {
    //父子线程共享本地线程变量
    private static final ThreadLocal<HeaderInfo> THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * Sets header info.
     * @param headerInfo the header info
     */
//设置线程需要保存的值
    public static void setHeaderInfo(HeaderInfo headerInfo) {
        THREAD_LOCAL.set(headerInfo);
    }

    /**
     * Gets header info.
     * @return the header info
     */
//获取线程中保存的值
    public static HeaderInfo getHeaderInfo() {
        return THREAD_LOCAL.get();
    }

    /**
     * Remove header info.
     */
//移除线程中保存的值
    public static void removeHeaderInfo() {
        THREAD_LOCAL.remove();
    }
}
