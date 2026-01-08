package com.lightrpc.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务提供者注解
 * 作用：被此注解标记的类，会被 LightRPC 自动发布为服务
 */
@Target(ElementType.TYPE) // 作用在类
@Retention(RetentionPolicy.RUNTIME) // 运行时可见
@Component
public @interface LightRpcService {

    // 服务接口类（默认取实现的第一个接口，但指定一下更安全）
    Class<?> interfaceClass() default Void.class;

    // 版本号（预留扩展）
    String version() default "1.0";
}
