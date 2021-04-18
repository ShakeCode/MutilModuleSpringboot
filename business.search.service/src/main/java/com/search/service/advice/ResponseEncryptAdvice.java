package com.search.service.advice;

import com.search.service.model.ResultVO;
import com.search.service.utils.AesUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * The type Response encrypt advice.
 */
@ControllerAdvice
public class ResponseEncryptAdvice implements ResponseBodyAdvice<Object> {

    @Value("${module.boots.response.aes.key:xy934yrn9342u0ry4br8cn-9u2}")
    private String key;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // 判断是否需要加密
        final boolean encrypt = NeedEncrypt.checkIfNeedEncrypt(returnType);
        if (!encrypt) {
            return body;
        } else {
            // 如果body是属于ResponseMsg类型,只需要对data里面的数据进行加密即可
            if (body instanceof ResultVO) {
                ResultVO responseMsg = (ResultVO) body;
                Object data = responseMsg.getData();
                if (data == null) {
                    return body;
                } else {
                    responseMsg.setData(AesUtils.encrypt(data.toString(), key));
                    return responseMsg;
                }
            } else {
                return body;
            }
        }
    }

}