package com.search.service.config;

import com.search.service.utils.HeaderContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
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
        // 获取主子线程的请求头信息
        if (!StringUtils.isEmpty(HeaderContext.getHeaderInfo().getUserInfo())) {
            requestTemplate.header("user-info", HeaderContext.getHeaderInfo().getUserInfo());
        }

        if (!StringUtils.isEmpty(HeaderContext.getHeaderInfo().getTenantCode())) {
            requestTemplate.header("tenant-code", HeaderContext.getHeaderInfo().getTenantCode());
        }

        if (!StringUtils.isEmpty(HeaderContext.getHeaderInfo().getGcAuthentication())) {
            requestTemplate.header("gc-authentication", HeaderContext.getHeaderInfo().getGcAuthentication());
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
