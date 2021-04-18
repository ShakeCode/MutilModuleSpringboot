package com.search.service.advice;

import com.search.service.annotation.ResponseEncrypt;
import org.springframework.core.MethodParameter;

/**
 * The type Need encrypt.
 */
public class NeedEncrypt {
    /**
     * Check if need encrypt boolean.
     * @param returnType the return type
     * @return the boolean
     */
    public static boolean checkIfNeedEncrypt(MethodParameter returnType) {
        // 获取类上的注解
        boolean classPresentAnno = returnType.getContainingClass().isAnnotationPresent(ResponseEncrypt.class);
        // 获取方法上的注解
        boolean methodPresentAnno = returnType.getMethod().isAnnotationPresent(ResponseEncrypt.class);
        boolean encrypt = false;
        if (classPresentAnno) {
            // 类上标注的是否需要加密
            encrypt = returnType.getContainingClass().getAnnotation(ResponseEncrypt.class).value();
            // 类不加密，所有都不加密
            if (!encrypt) {
                return false;
            }
        }
        if (methodPresentAnno) {
            // 方法上标注的是否需要加密
            encrypt = returnType.getMethod().getAnnotation(ResponseEncrypt.class).value();
        }
        return encrypt;
    }

}
