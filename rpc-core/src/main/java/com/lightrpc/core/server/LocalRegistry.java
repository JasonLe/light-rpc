package com.lightrpc.core.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalRegistry {
    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    public static void register(String interfaceName, Object serviceBean) {
        serviceMap.put(interfaceName, serviceBean);
    }

    public static Object get(String interfaceName) {
        return serviceMap.get(interfaceName);
    }
}
