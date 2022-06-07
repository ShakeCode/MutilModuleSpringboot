package com.redis.service.service;

import com.redis.service.annotation.LogTime;
import com.redis.service.annotation.RedisLock;
import com.redis.service.model.ResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

/**
 * Spring framework 4.x开始支持注解嵌套。4.X之前支持两层注解。
 * 【原因】:
 * 1、4.X 之前采用了两层for循环。
 * 2、4.X之后采用了递归调用
 * <p>
 * 切面生效在public方法, 可以注解上多个, 不行的话新开类开public方法!!!!!
 * <p>
 * 在单个类内的方法调用是不能够进入切面中的，这就说明在内部方法调用时并未使用代理对象进行代理.
 * <p>
 * 【原因】：由于是在本类的内部方法之间进行调用，所以肯定是调用的当前类this对象，断点调试下，可以发现这个this对象并不是代理对象，而只是当前类的普通对象，因此不存在方法增强。
 * 在使用Spring AOP的时候，我们从IOC容器中获取的Bean对象其实都是代理对象，而不是那些Bean对象本身，由于this关键字引用的并不是该Service Bean对象的代理对象，而是其本身，因此Spring AOP是不能拦截到这些被嵌套调用的方法的。
 * <p>
 * 【解决方案】：
 * 1、修改类，不要出现“自调用”的情况：这是Spring文档中推荐的“最佳”方案；
 * 2、若一定要使用“自调用”，那么this.doSomething2()替换为：((CustomerService)
 * AopContext.currentProxy()).doSomething2()；
 * <p>
 * 3、
 * 既然了解了整个过程，那么我们只需要使得在内部方法调用时拿到当前类的代理对象，然后再用这个代理对象去执行目标方法不就ok啦？这里就提供一种简单的方式来实现，运用AopContext类来获取当前类的代理对象，当然这里有个前提，必须要在启动类上加上@EnableAspectJAutoProxy(exposeProxy = true)，后面的参数即代表在进行内部方法间的调用时如果也想能够进行代理，那么必须将该参数置为true，否则会报错（Cannot find current proxy: Set 'exposeProxy' property on Advised to 'true' to make it available.）！！！默认为false，当然在该注解上也可以进行proxy-target-class参数的配置，
 * 形如：
 * @EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
 * ————————————————
 * <p>
 * <p>
 * https://blog.csdn.net/u012760435/article/details/108494713
 * https://blog.csdn.net/m0_38001814/article/details/97921849
 * <p>
 * 【关于注解 @EnableAspectJAutoProxy 官方解释】:
 * https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/annotation/EnableAspectJAutoProxy.html#exposeProxy--
 * @EnableAspectJAutoProxy 参数:
 * exposeProxy
 * 指示代理应由 AOP 框架公开为ThreadLocal 用于通过AopContext类检索。
 * proxyTargetClass
 * 指示是否要创建基于子类 (CGLIB) 的代理，而不是基于标准 Java 接口的代理
 * <p>
 * 【注意点】:
 * 1.AopContext.currentProxy()的本质是使用的ThreadLocal生成本地代理，这样的做法可能影响性能。
 * 2.被代理对象使用的方法必须是public类型的方法，不然获取不到代理对象,会报下面的错误：
 * java.lang.IllegalStateException: Cannot find current proxy: Set 'exposeProxy' property on Advised to 'true' to make it available.
 * <p>
 * <p>
 * 怎么是个CGLIB代理对象呢？反正我最开始很纳闷，这个目标对象明明实现了Service接口，按理说Spring应该采用JDK动态代理呀，怎么会去使用CGLIB动态代理呢？
 * <p>
 * 这就涉及到了另外一个配置参数，proxy-target-class，如果将该参数置为true，那么则强制使用基于类的代理进行创建，反之则基于接口的方式创建，而另一个坑是因为SpringBoot 2.x以后该参数被默认置为true了，（在Spring中默认是为false的
 * <p>
 * 在SpringBoot中如果你想启用jdk动态代理，可在yaml中去掉强制使用cglib代理的配置，如下：
 * <p>
 * spring:
 * # 默认为true，表示基于类的代理，如果为false表示基于jdk接口的代理
 * aop:
 * proxy-target-class: true
 * <p>
 * 当你改为false之后，你会发现controller层中的helloService会注入失败，那是因为基于接口形式的代理必须要以其实现的接口类型注入即HelloService而不能是原先的HelloServiceImpl，否则会一直报注入失败的问题，修改完毕后让我们再次断点查看，发现此时的helloService已经变成jdk代理对象啦~
 * The type Sms quota service.
 */
@Service
public class SmsQuotaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsQuotaService.class);

    private int quota = 100;

    /**
     * Check sms quota result vo.此处注解生效
     * @return the result vo
     */
    @LogTime(operationMessage = "check sms quota service ")
    @RedisLock(prefix = "checkSmsQuota", lockKey = "checkSmsQuota", waitTime = 3000, leaseTime = 6000)
    public ResultVO<String> checkSmsQuota() {
        // 继续的内部方法注解都失效,未引用代理对象进行代理
        // this.validCheck();
        // this.quotaCheck();

        // 强制获取代理对象后方法的注解全部生效!!!(耗时日志,加锁)
        getSmsQuotaService().validCheck();
        getSmsQuotaService().quotaCheck();
        return ResultVO.success("check sms quota success");
    }

    @LogTime(operationMessage = "quotaCheck")
    @RedisLock(prefix = "quotaCheck", lockKey = "quotaLock", waitTime = 3000, leaseTime = 6000)
    public void quotaCheck() {
        quota--;
        LOGGER.info("配额抢占成功,剩余配额:{}", quota);
    }

    @LogTime(operationMessage = "validCheck")
    public void validCheck() {
        LOGGER.info("validCheck....");
    }

    /**
     * 强制获取代理对象，必须开启exposeProxy配置，否则获取不到当前代理对象
     * @return SmsQuotaService
     */
    private SmsQuotaService getSmsQuotaService() {
        return AopContext.currentProxy() != null ? (SmsQuotaService) AopContext.currentProxy() : this;
    }
}
