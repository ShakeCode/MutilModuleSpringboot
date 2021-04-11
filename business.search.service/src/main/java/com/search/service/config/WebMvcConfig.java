package com.search.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebMvcConfig.class);

    // 本地存储路径,必须是/结尾
    // 访问路径: http://127.0.0.1:8091/search/songListPic/109951162869937004.jpg
    String locationPath = "F:/BaiduYunDownload/data/data/img/songListPic/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        LOGGER.info("本地图片存储路径：" + locationPath);
        registry.addResourceHandler("/songListPic/**").addResourceLocations("file:" + locationPath);
//        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }
}
