package com.lightrpc.test;

import com.lightrpc.core.spring.SpringRpcClientBean;
import com.lightrpc.registry.impl.NacosServiceRegistry;
import com.lightrpc.registry.ServiceRegistry;
import com.lightrpc.test.controller.UserController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.lightrpc.test.controller") // 扫描 UserController
public class SpringTestClient {

    @Bean
    public SpringRpcClientBean springRpcClientBean() {
        // 1. 配置注册中心
        ServiceRegistry registry = new NacosServiceRegistry("127.0.0.1:8848");

        // 2. 返回处理 @LightRpcClient 的 BeanPostProcessor
        return new SpringRpcClientBean(registry);
    }

    public static void main(String[] args) {
        // 启动 Spring 容器
        ApplicationContext context = new AnnotationConfigApplicationContext(SpringTestClient.class);

        // 取出 UserController
        UserController userController = context.getBean(UserController.class);

        // 调用测试方法
        userController.test();
    }
}