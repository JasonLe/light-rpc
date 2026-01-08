package com.lightrpc.test;

import com.lightrpc.core.spring.SpringRpcProviderBean;
import com.lightrpc.registry.impl.NacosServiceRegistry;
import com.lightrpc.registry.ServiceRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.lightrpc.test.impl") // 扫描具体的服务实现类
public class SpringTestServer {

    @Bean
    public SpringRpcProviderBean springRpcProviderBean() {
        // 1. 配置注册中心
        ServiceRegistry registry = new NacosServiceRegistry("127.0.0.1:8848");

        // 2. 返回我们的 Spring 集成 Bean
        // 指定本机 IP 和端口 6666
        return new SpringRpcProviderBean("127.0.0.1", 6666, registry);
    }

    public static void main(String[] args) {
        // 启动 Spring 容器
        new AnnotationConfigApplicationContext(SpringTestServer.class);
        // 之后什么都不用做了，Netty 会自动启动，服务会自动注册
    }
}