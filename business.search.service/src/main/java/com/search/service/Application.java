package com.search.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;


/**
 * 四种方式
 * 操作es的主要有四种方式:
 * 第一种 :spring date es ,这种方式简单,多用于一些简单业务,但因为方法都帮你封装好了,灵活度不高!(复杂业务不推荐)
 * <p>
 * 第二种 :transportClient ,这种方式，官方已经明确表示在ES 7.0版本中将弃用TransportClient客户端，且在8.0版本中完全移除它
 * <p>
 * 第三种 :REST Client 这种方式是基于http 与 es 通信,方便(官网推荐),主要有restHighLevelClient 和restlowLevelClient俩版本,这里也是我是选用的方式. 任何版本都支持REST Client(restlowLevelClient版本)
 * <p>
 * 第四种：Http restful api接口调用，elaticsearch-head常用方式
 * <p>
 * The type Application.
 * 2020-09-15
 */
@EnableFeignClients
@EnableAsync
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
