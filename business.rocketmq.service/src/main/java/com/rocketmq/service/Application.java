package com.rocketmq.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


/**
 * 服务端下载： http://rocketmq.apache.org/dowloading/releases/
 * 控制台： https://github.com/apache/rocketmq-externals/tree/master/rocketmq-connect-console
 * 入门实例： https://github.com/apache/rocketmq/blob/master/docs/cn/RocketMQ_Example.md#11-%E5%8A%A0%E5%85%A5%E4%BE%9D%E8%B5%96
 * 入门：https://blog.csdn.net/zxl646801924/article/details/105659481
 * The type Application.
 */
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
