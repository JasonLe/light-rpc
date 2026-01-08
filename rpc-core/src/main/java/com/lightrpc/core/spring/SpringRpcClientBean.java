package com.lightrpc.core.spring;

import com.lightrpc.core.annotation.LightRpcClient;
import com.lightrpc.core.proxy.RpcClientProxy;
import com.lightrpc.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

/**
 * Spring 集成核心类 (客户端)
 * 负责扫描 @LightRpcClient 注解，自动注入代理对象
 */
@Slf4j
public class SpringRpcClientBean implements BeanPostProcessor {

    private final RpcClientProxy rpcClientProxy;

    public SpringRpcClientBean(ServiceRegistry serviceRegistry) {
        // 在这里初始化代理工厂
        this.rpcClientProxy = new RpcClientProxy(serviceRegistry);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        // 遍历所有字段 (包括私有字段)
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            // 检查字段上是否有 @LightRpcClient 注解
            if (field.isAnnotationPresent(LightRpcClient.class)) {
                try {
                    // 1. 获取字段类型 (接口类型，如 UserService)
                    Class<?> interfaceClass = field.getType();

                    // 2. 使用代理工厂生成代理对象
                    Object proxy = rpcClientProxy.getProxy(interfaceClass);

                    // 3. 暴力反射注入 (因为字段通常是 private 的)
                    field.setAccessible(true);
                    field.set(bean, proxy);

                    log.info("【Spring自动注入】已为 Bean [{}] 的字段 [{}] 注入代理对象", beanName, field.getName());

                } catch (IllegalAccessException e) {
                    log.error("字段注入失败", e);
                }
            }
        }
        return bean;
    }
}