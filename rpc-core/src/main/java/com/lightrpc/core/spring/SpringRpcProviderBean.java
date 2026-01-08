package com.lightrpc.core.spring;

import com.lightrpc.core.annotation.LightRpcService;
import com.lightrpc.core.server.RpcServer;
import com.lightrpc.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Spring 集成核心类 (服务端)
 * 1. 负责扫描 @LightRpcService 注解，自动发布服务
 * 2. 负责在 Spring 启动完毕后，启动 Netty 服务端
 */
@Slf4j
public class SpringRpcProviderBean implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    private final RpcServer rpcServer;

    public SpringRpcProviderBean(String host, int port, ServiceRegistry registry) {
        // 在构造时就创建好 RpcServer，但先不 start
        this.rpcServer = new RpcServer(host, port, registry);
    }

    /**
     * Bean 初始化后调用
     * 我们在这里拦截 @LightRpcService，进行服务注册
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Class<?> beanClass = bean.getClass();

        if (beanClass.isAnnotationPresent(LightRpcService.class)) {
            LightRpcService lightRpcService = beanClass.getAnnotation(LightRpcService.class);

            // 获取接口名称
            Class<?> interfaceClass = lightRpcService.interfaceClass();
            if (interfaceClass == Void.class) {
                interfaceClass = beanClass.getInterfaces()[0];
            }
            String serviceName = interfaceClass.getName();

            rpcServer.publishService(serviceName, bean);
            log.info("【Spring自动注册】发现服务 【{}】，已自动注册", serviceName);
        }
        return bean;
    }

    /**
     * Spring 容器启动完成后调用
     * 我们在这里启动 Netty Server
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 防止 Spring 父子容器导致多次启动
        if (event.getApplicationContext().getParent() == null) {
            new Thread(rpcServer::start).start();
        }
    }
}
