package com.redis.service.utils;

import com.redis.service.model.HeaderInfo;

public class ThreadHeaderLocalUtil {
    //父子线程共享本地线程变量
    private static final ThreadLocal<HeaderInfo> THREAD_LOCAL = new InheritableThreadLocal<>();

    //设置线程需要保存的值
    public static void setHeaderInfo(HeaderInfo headerInfo) {
        THREAD_LOCAL.set(headerInfo);
    }

    //获取线程中保存的值
    public static HeaderInfo getHeaderInfo() {
        return THREAD_LOCAL.get();
    }

    //移除线程中保存的值
    public static void removeHeaderInfo() {
        THREAD_LOCAL.remove();
    }
}
