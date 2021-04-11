package com.search.service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * The type Feign interceptor.
 */
public class FeignInterceptor implements RequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeignInterceptor.class);

    private static final String[] headers = new String[]{"tenent-code", "gc-authentication", "user-info"};

    @Override
    public void apply(RequestTemplate requestTemplate) {
        LOGGER.info(">>feign interceptor...");
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    String values = request.getHeader(name);
                    if (iscopy(name)) {
                        LOGGER.info("Set Header...Name:{}", name);
                        requestTemplate.header(name, values);
                    }
                }
            }
        }
    }

    /**
     * @param name
     * @return
     */
    private Boolean iscopy(String name) {
        for (String header : headers) {
            if (header.equals(name)) {
                return true;
            }
        }
        return false;
    }
}
