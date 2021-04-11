package com.search.service.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Feign config.
 */
@Configuration
public class FeignConfig {

    /**
     * Feign builder feign . builder.
     * @return the feign . builder
     */
    @Bean
    public Feign.Builder feignBuilder() {
        return Feign.builder();
    }

    /**
     * Feign contract contract.
     * @return the contract
     */
  /*  @Bean
    public Contract feignContract() {
        return new feign.Contract.Default();
    }
*/

    /**
     * Feign request options request . options.
     * @return the request . options
     */
    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(10000, 60000);
    }

    /**
     * Feign retry retryer.
     * @return the retryer
     */
    @Bean
    public Retryer feignRetry() {
        return new Retryer.Default();
    }

    /**
     * 开启feign调用日志打印的debug模式输出的内容级别,需要将client类包路径日志级别设置为debug
     * @return logger . level
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

/*    @Bean
    public SpringEncoder feignEncoder(){
        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(this.createFastJsonConverter());
        return new SpringEncoder(objectFactory);
    }*/

    private HttpMessageConverter createFastJsonConverter() {
        //创建fastJson消息转换器
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();

        //升级最新版本需加=============================================================
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_ATOM_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        supportedMediaTypes.add(MediaType.APPLICATION_PDF);
        supportedMediaTypes.add(MediaType.APPLICATION_RSS_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XHTML_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XML);
        supportedMediaTypes.add(MediaType.IMAGE_GIF);
        supportedMediaTypes.add(MediaType.IMAGE_JPEG);
        supportedMediaTypes.add(MediaType.IMAGE_PNG);
        supportedMediaTypes.add(MediaType.TEXT_EVENT_STREAM);
        supportedMediaTypes.add(MediaType.TEXT_HTML);
        supportedMediaTypes.add(MediaType.TEXT_MARKDOWN);
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
        supportedMediaTypes.add(MediaType.TEXT_XML);

        fastConverter.setSupportedMediaTypes(supportedMediaTypes);

        //创建配置类
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        //修改配置返回内容的过滤
        //WriteNullListAsEmpty  ：List字段如果为null,输出为[],而非null
        //WriteNullStringAsEmpty ： 字符类型字段如果为null,输出为"",而非null
        //DisableCircularReferenceDetect ：消除对同一对象循环引用的问题，默认为false（如果不配置有可能会进入死循环）
        //WriteNullBooleanAsFalse：Boolean字段如果为null,输出为false,而非null
        //WriteMapNullValue：是否输出值为null的字段,默认为false
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteMapNullValue
        );
        fastConverter.setFastJsonConfig(fastJsonConfig);
        return fastConverter;
    }

    /**
     * feign请求拦截器
     * @return request interceptor
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new FeignInterceptor();
    }
}
