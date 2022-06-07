package com.redis.service.constant;

/**
 * The type Constant.
 */
public class Constant {

    /**
     * The constant COMMA.
     */
    public static final String COMMA = ",";

    /**
     * The constant COLON.
     */
    public static final String COLON = ":";

    /**
     * The constant UNDER_LINE.
     */
    public static final String UNDER_LINE = "_";

    /**
     * The interface Header param.
     */
    public interface HeaderParam {
        /**
         * The constant tenantCode.
         */
        String tenantCode = "tenent-code";
    }

    /**
     * The interface Redis mode.
     */
    public interface RedisMode {
        /**
         * The constant single.
         */
        String SINGLE = "single";
        /**
         * The constant sentinel.
         */
        String SENTINEL = "sentinel";
        /**
         * The constant cluster.
         */
        String CLUSTER = "cluster";
    }
}
