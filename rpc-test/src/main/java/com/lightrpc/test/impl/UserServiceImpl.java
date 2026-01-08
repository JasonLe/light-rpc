package com.lightrpc.test.impl;

import com.lightrpc.api.user.UserService;
import com.lightrpc.core.annotation.LightRpcService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LightRpcService
public class UserServiceImpl implements UserService {
    @Override
    public String getUser(String username) {
        log.info("【服务端】UserServiceImpl 收到查询请求，参数 name: {}", username);
        return username;
    }
}
