package com.redis.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * The type Application.
 * @EnableAspectJAutoProxy 参数 : exposeProxy 指示代理应由 AOP 框架公开为ThreadLocal 用于通过AopContext类检索。 proxyTargetClass 指示是否要创建基于子类 (CGLIB) 的代理，而不是基于标准 Java 接口的代理
 */
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
@EnableCaching
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {
    /**
     * The entry point of application.
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
