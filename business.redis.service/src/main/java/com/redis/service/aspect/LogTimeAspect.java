package com.redis.service.aspect;

import com.redis.service.annotation.LogTime;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * The type Log time aspect.
 */
@Component
@Aspect
public class LogTimeAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogTimeAspect.class);


    /**
     * Point cut.
     */
    @Pointcut("@annotation(com.redis.service.annotation.LogTime)")
    public void pointCut() {
    }

    /**
     * Around object.
     * @param pjp the pjp
     * @return the object
     * @throws Throwable the throwable
     */
    @Around("pointCut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 获取方法名(类全路径+.+方法名)
        // String classFullName = pjp.getSignature().getDeclaringTypeName();
        // String className = classFullName.substring(classFullName.lastIndexOf(".") + 1);
        // 比如: com.xxx.XxxService.xxxMethod
        // String name = className + "." + pjp.getSignature().getName();
        LogTime logTime = getLogTimeAnnotation(pjp);
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        // 记录结束时间和用时
        LOGGER.info("{},waste time:{}ms", logTime.operationMessage(), (System.currentTimeMillis() - start));
        return result;
    }

    private LogTime getLogTimeAnnotation(ProceedingJoinPoint pjp) {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        return method.getAnnotation(LogTime.class);
    }


    private LogTime getMethodDescription(JoinPoint joinPoint) throws Exception {
        // 类名
        String targetName = joinPoint.getTarget().getClass().getName();
        // 方法名
        String methodName = joinPoint.getSignature().getName();
        // 参数
        Object[] arguments = joinPoint.getArgs();
        // 通过反射获取示例对象
        Class<?> targetClass = Class.forName(targetName);
        // 通过实例对象方法数组
        Method[] methods = targetClass.getMethods();
        LogTime logTime = null;
        for (Method method : methods) {
            // 判断方法名是不是一样
            if (method.getName().equals(methodName)) {
                // 对比参数数组的长度
                Class<?>[] clazzs = method.getParameterTypes();
                if (clazzs.length == arguments.length) {
                    // 获取注解里的日志信息
                    logTime = method.getAnnotation(LogTime.class);
                    break;
                }
            }
        }
        return logTime;
    }

}
