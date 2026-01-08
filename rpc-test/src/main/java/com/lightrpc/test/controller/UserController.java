package com.lightrpc.test.controller;

import com.lightrpc.api.user.UserService;
import com.lightrpc.core.annotation.LightRpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component // 让它成为一个 Spring Bean
public class UserController {

    // --- 核心：自动注入远程代理 ---
    @LightRpcClient
    private UserService userService;

    public void test() {
        String result = userService.getUser("Spring-Auto-Inject");
        log.info("远程调用结果: {}", result);
    }
}