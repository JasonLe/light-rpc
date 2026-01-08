package com.lightrpc.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务消费者注解
 * 作用：被此注解标记的字段，LightRPC 会自动注入代理对象
 */
@Target(ElementType.FIELD) // 作用在字段上
@Retention(RetentionPolicy.RUNTIME)
public @interface LightRpcClient {
    // 版本号
    String version() default "1.0";
}
