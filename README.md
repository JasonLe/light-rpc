# LightRpc

[English](./README_EN.md) | 简体中文

## 项目简介

LightRpc 是一个基于 Netty 的轻量级、高性能 RPC 框架，支持服务注册发现、负载均衡、异步调用等特性，并提供了 Spring 无缝集成方案。

### 核心特性

- 自定义高效二进制通信协议
- 基于 Netty 的异步非阻塞 IO
- 集成 Nacos 服务注册与发现
- 支持多种序列化协议（JSON、Protobuf 可扩展）
- 客户端连接池复用
- Spring 注解式开发，开箱即用
- 负载均衡（随机策略，可扩展）
- 心跳检测与自动重连机制

## 架构设计

### 模块结构

```
light-rpc
├── rpc-api          # API 接口层
├── rpc-common       # 公共组件（协议、序列化、枚举）
├── rpc-core         # 核心实现（客户端、服务端、编解码、代理）
├── rpc-registry     # 注册中心（Nacos 实现）
└── rpc-test         # 测试示例
```

### 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Netty | 4.1.101.Final | 高性能网络通信框架 |
| Nacos | 2.2.0 | 服务注册与发现 |
| Gson | 2.13.2 | JSON 序列化 |
| Spring | 5.3.6 | 依赖注入与生命周期管理 |
| Lombok | 1.18.30 | 简化 Java 代码 |
| SLF4J + Logback | 1.7.36 + 1.2.12 | 日志框架 |

### RPC 协议设计

```
+-------+--------+------------+-----------+------------+----------------+
| Magic | Version | Serializer | Msg Type | Request ID | Data Length   |
| 4B    | 1B      | 1B         | 1B       | 8B         | 4B            |
+-------+--------+------------+-----------+------------+----------------+
|                          Data Body                                    |
|                      (序列化后的请求/响应)                               |
+-----------------------------------------------------------------------+
```

**协议字段说明**：
- **Magic Number**：`0xCAFEBABE`，魔数校验
- **Version**：协议版本号，支持协议升级
- **Serializer**：序列化类型（1=JSON，2=Protobuf）
- **Msg Type**：消息类型（0=心跳，1=请求，2=响应）
- **Request ID**：请求唯一标识，用于异步匹配
- **Data Length**：消息体长度，解决 TCP 粘包问题

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- Nacos Server 2.x（需提前启动）

### 启动 Nacos

1. 下载 Nacos 2.2.0 并解压
2. 进入 `nacos/bin` 目录
3. 执行 `sh startup.sh -m standalone` 启动单机模式
4. 访问控制台：http://localhost:8848/nacos
5. 默认用户名密码：nacos/nacos

### 使用步骤

#### 1. 定义服务接口

在 `rpc-api` 模块中定义服务接口。

#### 2. 服务端实现

- 在实现类上添加 `@LightRpcService` 注解
- 配置 Spring 扫描服务实现类
- 创建 `SpringRpcProviderBean` 并配置 Nacos 地址和服务端口
- 启动 Spring 容器，服务端自动启动并注册到 Nacos

#### 3. 客户端调用

- 在需要调用远程服务的字段上添加 `@LightRpcClient` 注解
- 配置 Spring 扫描控制器类
- 创建 `SpringRpcClientBean` 并配置 Nacos 地址
- 启动 Spring 容器，自动注入远程代理对象
- 像调用本地方法一样调用远程服务

完整示例请参考 `rpc-test` 模块中的测试代码。

## 核心功能详解

### 1. 服务注册与发现

基于 Nacos 实现服务注册与发现，支持自动注册和负载均衡。

**特性**：
- 服务端启动时自动注册到 Nacos
- 客户端调用时自动从 Nacos 获取服务地址
- 注册失败自动重试（最多 5 次）
- 随机负载均衡策略
- 可扩展轮询、一致性哈希等策略

### 2. 连接池管理

客户端自动复用连接，避免频繁建立 TCP 连接。

**特性**：
- 同一服务地址只创建一次连接
- 使用 ConcurrentHashMap 保证线程安全
- 连接复用减少三次握手/四次挥手开销

### 3. 异步调用

基于 CompletableFuture 实现异步非阻塞调用。

**优势**：
- 不阻塞业务线程
- 支持高并发场景
- 基于 Request ID 精确匹配请求和响应
- 性能优秀，吞吐量高

### 4. 心跳检测

服务端自动检测客户端连接状态。

**特性**：
- 10 秒无数据读取，自动关闭连接
- 客户端可配置写空闲主动发送心跳包（预留）
- 防止无效连接占用资源

### 5. 序列化扩展

支持多种序列化协议，可灵活切换。

**特性**：
- 当前支持 JSON 序列化
- 预留 Protobuf、Kryo 等扩展接口
- 通过协议字段区分序列化类型
- 可根据场景选择最优序列化方案

## RPC 调用流程

```
客户端                     注册中心                     服务端
  |                         |                           |
  | 1. 调用代理方法          |                           |
  |------------------------>|                           |
  |                         |                           |
  | 2. 服务发现（Nacos）     |                           |
  |------------------------>|                           |
  |<------ 返回服务地址 -----|                           |
  |                         |                           |
  | 3. 获取连接（连接池）     |                           |
  |                         |                           |
  | 4. 编码并发送请求        |                           |
  |--------------------------------------------------------->|
  |                         |                    5. 解码   |
  |                         |                    6. 反射调用|
  |                         |                    7. 编码响应|
  | 8. 接收响应             |                           |
  |<---------------------------------------------------------|
  |                         |                           |
  | 9. 反序列化并返回结果    |                           |
  |<------------------------|                           |
```

## 项目亮点

### 架构设计

- **清晰的模块划分**：API、Common、Core、Registry 职责明确
- **面向接口编程**：低耦合高内聚，易于维护
- **遵循开闭原则**：对扩展开放，对修改关闭
- **依赖倒置原则**：依赖抽象而非具体实现

### 技术实现

- **自定义协议**：高效二进制协议，协议头仅 19 字节
- **协议版本化**：支持协议升级和向后兼容
- **零拷贝优化**：基于 Netty 的零拷贝和直接内存
- **异步非阻塞**：基于 Netty 的事件驱动模型
- **连接复用**：连接池减少连接开销
- **完善日志**：全链路日志记录，便于排查问题

### Spring 集成

- **BeanPostProcessor**：自动扫描并注册 RPC 服务
- **ApplicationListener**：监听容器启动事件
- **注解驱动**：`@LightRpcService` 和 `@LightRpcClient`
- **开箱即用**：零配置文件，纯 Java 配置

### 可扩展性

- **序列化协议**：可扩展 JSON、Protobuf、Kryo 等
- **注册中心**：可扩展 Nacos、Zookeeper、Consul 等
- **负载均衡**：可扩展随机、轮询、一致性哈希等
- **压缩算法**：预留压缩字段，可扩展 Gzip、Snappy 等

## 性能优化

### 1. Netty 参数调优

- **TCP_NODELAY**：禁用 Nagle 算法，降低延迟
- **SO_KEEPALIVE**：开启 TCP 层心跳检测
- **SO_BACKLOG**：增大连接队列，提高并发处理能力

### 2. 连接复用

- 客户端连接池避免频繁握手
- 减少三次握手/四次挥手开销
- 提升系统整体性能

### 3. 异步处理

- 使用 CompletableFuture 实现异步调用
- 不阻塞业务线程
- 提高系统吞吐量

### 4. 协议优化

- 协议头仅 19 字节，开销小
- 二进制协议比 HTTP+JSON 更高效
- 支持批量请求和响应

## 设计模式

- **代理模式**：RpcClientProxy 透明调用远程服务
- **工厂模式**：RpcClientFactory 管理客户端连接
- **策略模式**：可插拔的序列化算法
- **观察者模式**：Spring 事件监听机制
- **模板方法模式**：Netty 的 ChannelHandler 处理流程

## 后续优化方向

- [ ] 支持 Protobuf、Kryo 序列化
- [ ] 实现 Gzip、Snappy 压缩
- [ ] 增加轮询、最小连接数等负载均衡策略
- [ ] 集成 Sentinel 实现限流熔断
- [ ] 集成 SkyWalking 实现链路追踪
- [ ] 支持服务降级与灰度发布
- [ ] 支持 HTTP/2、gRPC 协议
- [ ] 实现客户端主动发送心跳
- [ ] 支持服务版本管理
- [ ] 增加监控指标和健康检查

## 项目结构

```
light-rpc
├── pom.xml                           # 父 POM
├── rpc-api                           # API 层
│   └── src/main/java/com/lightrpc/api
│       └── user/UserService.java    # 服务接口
├── rpc-common                        # 公共层
│   └── src/main/java/com/lightrpc/common
│       ├── enums                     # 枚举类
│       │   ├── MessageTypeEnum.java
│       │   └── SerializerCodeEnum.java
│       ├── model                     # 数据模型
│       │   ├── RpcMessage.java
│       │   ├── RpcRequest.java
│       │   └── RpcResponse.java
│       └── serializer                # 序列化
│           ├── Serializer.java
│           └── impl/JsonSerializer.java
├── rpc-core                          # 核心层
│   └── src/main/java/com/lightrpc/core
│       ├── annotation                # 注解
│       │   ├── LightRpcClient.java
│       │   └── LightRpcService.java
│       ├── client                    # 客户端
│       │   ├── RpcClient.java
│       │   ├── RpcClientHandler.java
│       │   └── UnprocessedRequests.java
│       ├── codec                     # 编解码器
│       │   ├── RpcMessageDecoder.java
│       │   └── RpcMessageEncoder.java
│       ├── proxy                     # 动态代理
│       │   ├── RpcClientFactory.java
│       │   └── RpcClientProxy.java
│       ├── server                    # 服务端
│       │   ├── LocalRegistry.java
│       │   ├── RpcServer.java
│       │   └── RpcServerHandler.java
│       └── spring                    # Spring 集成
│           ├── SpringRpcClientBean.java
│           └── SpringRpcProviderBean.java
├── rpc-registry                      # 注册中心
│   └── src/main/java/com/lightrpc/registry
│       ├── ServiceRegistry.java
│       └── impl/NacosServiceRegistry.java
└── rpc-test                          # 测试示例
    └── src/main/java/com/lightrpc/test
        ├── NettyTestClient.java      # 原生 Netty 测试
        ├── NettyTestServer.java
        ├── SpringTestClient.java     # Spring 集成测试
        ├── SpringTestServer.java
        ├── controller/UserController.java
        └── impl/UserServiceImpl.java
```

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

[MIT License](LICENSE)

## 联系方式

如有问题或建议，欢迎通过 GitHub Issues 联系。
