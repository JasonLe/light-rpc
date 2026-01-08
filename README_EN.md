# LightRpc

English | [简体中文](./README.md)

## Introduction

LightRpc is a lightweight, high-performance RPC framework based on Netty, supporting service registration and discovery, load balancing, asynchronous calls, and seamless Spring integration.

### Key Features

- Custom efficient binary communication protocol
- Asynchronous non-blocking IO based on Netty
- Integrated Nacos service registration and discovery
- Multiple serialization protocols support (JSON, Protobuf extensible)
- Client connection pooling
- Spring annotation-based development, ready to use out of the box
- Load balancing (random strategy, extensible)
- Heartbeat detection and auto-reconnection

## Architecture

### Module Structure

```
light-rpc
├── rpc-api          # API interface layer
├── rpc-common       # Common components (protocol, serialization, enums)
├── rpc-core         # Core implementation (client, server, codec, proxy)
├── rpc-registry     # Registry center (Nacos implementation)
└── rpc-test         # Test examples
```

### Technology Stack

| Technology | Version | Description |
|------------|---------|-------------|
| Netty | 4.1.101.Final | High-performance network framework |
| Nacos | 2.2.0 | Service registration and discovery |
| Gson | 2.13.2 | JSON serialization |
| Spring | 5.3.6 | Dependency injection and lifecycle management |
| Lombok | 1.18.30 | Simplify Java code |
| SLF4J + Logback | 1.7.36 + 1.2.12 | Logging framework |

### RPC Protocol Design

```
+-------+--------+------------+-----------+------------+----------------+
| Magic | Version | Serializer | Msg Type | Request ID | Data Length   |
| 4B    | 1B      | 1B         | 1B       | 8B         | 4B            |
+-------+--------+------------+-----------+------------+----------------+
|                          Data Body                                    |
|                 (Serialized request/response)                         |
+-----------------------------------------------------------------------+
```

**Protocol Fields**:
- **Magic Number**: `0xCAFEBABE`, magic number verification
- **Version**: Protocol version, supports protocol upgrade
- **Serializer**: Serialization type (1=JSON, 2=Protobuf)
- **Msg Type**: Message type (0=Heartbeat, 1=Request, 2=Response)
- **Request ID**: Unique request identifier for async matching
- **Data Length**: Message body length, solves TCP sticky packet problem

## Quick Start

### Requirements

- JDK 1.8+
- Maven 3.6+
- Nacos Server 2.x (need to start in advance)

### Start Nacos

1. Download Nacos 2.2.0 and extract
2. Enter `nacos/bin` directory
3. Execute `sh startup.sh -m standalone` to start in standalone mode
4. Access console: http://localhost:8848/nacos
5. Default username/password: nacos/nacos

### Usage Steps

#### 1. Define Service Interface

Define service interfaces in the `rpc-api` module.

#### 2. Server Implementation

- Add `@LightRpcService` annotation to the implementation class
- Configure Spring to scan service implementation classes
- Create `SpringRpcProviderBean` and configure Nacos address and server port
- Start Spring container, server starts automatically and registers to Nacos

#### 3. Client Invocation

- Add `@LightRpcClient` annotation to fields that need to invoke remote services
- Configure Spring to scan controller classes
- Create `SpringRpcClientBean` and configure Nacos address
- Start Spring container, remote proxy objects are automatically injected
- Invoke remote services like calling local methods

See test code in the `rpc-test` module for complete examples.

## Core Features

### 1. Service Registration and Discovery

Based on Nacos for service registration and discovery with automatic registration and load balancing.

**Features**:
- Server automatically registers to Nacos on startup
- Client automatically retrieves service addresses from Nacos on invocation
- Auto-retry on registration failure (up to 5 times)
- Random load balancing strategy
- Extensible for round-robin, consistent hashing, etc.

### 2. Connection Pool Management

Client automatically reuses connections to avoid frequent TCP connection establishment.

**Features**:
- Create connection only once for the same service address
- Thread-safe using ConcurrentHashMap
- Connection reuse reduces three-way handshake/four-way wave overhead

### 3. Asynchronous Invocation

Asynchronous non-blocking calls based on CompletableFuture.

**Advantages**:
- Does not block business threads
- Supports high concurrency scenarios
- Precise matching of requests and responses based on Request ID
- Excellent performance and high throughput

### 4. Heartbeat Detection

Server automatically detects client connection status.

**Features**:
- Auto-close connection after 10 seconds without data read
- Client can configure write idle to send heartbeat packets (reserved)
- Prevent invalid connections from occupying resources

### 5. Serialization Extension

Supports multiple serialization protocols with flexible switching.

**Features**:
- Currently supports JSON serialization
- Reserved extension interfaces for Protobuf, Kryo, etc.
- Distinguish serialization types through protocol fields
- Choose optimal serialization scheme based on scenarios

## RPC Call Flow

```
Client                    Registry                    Server
  |                         |                           |
  | 1. Invoke proxy method  |                           |
  |------------------------>|                           |
  |                         |                           |
  | 2. Service discovery    |                           |
  |------------------------>|                           |
  |<-- Return service addr -|                           |
  |                         |                           |
  | 3. Get connection (pool)|                           |
  |                         |                           |
  | 4. Encode and send req  |                           |
  |--------------------------------------------------------->|
  |                         |                    5. Decode |
  |                         |               6. Reflect call|
  |                         |              7. Encode response|
  | 8. Receive response     |                           |
  |<---------------------------------------------------------|
  |                         |                           |
  | 9. Deserialize & return |                           |
  |<------------------------|                           |
```

## Project Highlights

### Architecture Design

- **Clear module division**: API, Common, Core, Registry with clear responsibilities
- **Interface-oriented programming**: Loose coupling and high cohesion, easy to maintain
- **Follows Open-Closed Principle**: Open for extension, closed for modification
- **Dependency Inversion Principle**: Depend on abstractions, not concrete implementations

### Technical Implementation

- **Custom protocol**: Efficient binary protocol with only 19-byte header
- **Protocol versioning**: Supports protocol upgrade and backward compatibility
- **Zero-copy optimization**: Netty-based zero-copy and direct memory
- **Asynchronous non-blocking**: Event-driven model based on Netty
- **Connection reuse**: Connection pool reduces connection overhead
- **Complete logging**: Full link logging for easy troubleshooting

### Spring Integration

- **BeanPostProcessor**: Automatically scan and register RPC services
- **ApplicationListener**: Listen to container startup events
- **Annotation-driven**: `@LightRpcService` and `@LightRpcClient`
- **Ready to use**: Zero configuration files, pure Java configuration

### Extensibility

- **Serialization protocols**: Extensible for JSON, Protobuf, Kryo, etc.
- **Registry center**: Extensible for Nacos, Zookeeper, Consul, etc.
- **Load balancing**: Extensible for random, round-robin, consistent hashing, etc.
- **Compression algorithms**: Reserved compression field for Gzip, Snappy, etc.

## Performance Optimization

### 1. Netty Parameter Tuning

- **TCP_NODELAY**: Disable Nagle algorithm, reduce latency
- **SO_KEEPALIVE**: Enable TCP-level heartbeat detection
- **SO_BACKLOG**: Increase connection queue size, improve concurrent processing capability

### 2. Connection Reuse

- Client connection pool avoids frequent handshakes
- Reduces three-way handshake/four-way wave overhead
- Improves overall system performance

### 3. Asynchronous Processing

- Uses CompletableFuture for async calls
- Does not block business threads
- Improves system throughput

### 4. Protocol Optimization

- Protocol header only 19 bytes, low overhead
- Binary protocol more efficient than HTTP+JSON
- Supports batch requests and responses

## Design Patterns

- **Proxy Pattern**: RpcClientProxy transparently invokes remote services
- **Factory Pattern**: RpcClientFactory manages client connections
- **Strategy Pattern**: Pluggable serialization algorithms
- **Observer Pattern**: Spring event listening mechanism
- **Template Method Pattern**: Netty's ChannelHandler processing flow

## Future Improvements

- [ ] Support Protobuf, Kryo serialization
- [ ] Implement Gzip, Snappy compression
- [ ] Add round-robin, least connections load balancing strategies
- [ ] Integrate Sentinel for rate limiting and circuit breaking
- [ ] Integrate SkyWalking for distributed tracing
- [ ] Support service degradation and canary deployment
- [ ] Support HTTP/2, gRPC protocols
- [ ] Implement client active heartbeat sending
- [ ] Support service version management
- [ ] Add monitoring metrics and health checks

## Project Structure

```
light-rpc
├── pom.xml                           # Parent POM
├── rpc-api                           # API layer
│   └── src/main/java/com/lightrpc/api
│       └── user/UserService.java    # Service interface
├── rpc-common                        # Common layer
│   └── src/main/java/com/lightrpc/common
│       ├── enums                     # Enums
│       │   ├── MessageTypeEnum.java
│       │   └── SerializerCodeEnum.java
│       ├── model                     # Data models
│       │   ├── RpcMessage.java
│       │   ├── RpcRequest.java
│       │   └── RpcResponse.java
│       └── serializer                # Serialization
│           ├── Serializer.java
│           └── impl/JsonSerializer.java
├── rpc-core                          # Core layer
│   └── src/main/java/com/lightrpc/core
│       ├── annotation                # Annotations
│       │   ├── LightRpcClient.java
│       │   └── LightRpcService.java
│       ├── client                    # Client
│       │   ├── RpcClient.java
│       │   ├── RpcClientHandler.java
│       │   └── UnprocessedRequests.java
│       ├── codec                     # Codec
│       │   ├── RpcMessageDecoder.java
│       │   └── RpcMessageEncoder.java
│       ├── proxy                     # Dynamic proxy
│       │   ├── RpcClientFactory.java
│       │   └── RpcClientProxy.java
│       ├── server                    # Server
│       │   ├── LocalRegistry.java
│       │   ├── RpcServer.java
│       │   └── RpcServerHandler.java
│       └── spring                    # Spring integration
│           ├── SpringRpcClientBean.java
│           └── SpringRpcProviderBean.java
├── rpc-registry                      # Registry center
│   └── src/main/java/com/lightrpc/registry
│       ├── ServiceRegistry.java
│       └── impl/NacosServiceRegistry.java
└── rpc-test                          # Test examples
    └── src/main/java/com/lightrpc/test
        ├── NettyTestClient.java      # Native Netty test
        ├── NettyTestServer.java
        ├── SpringTestClient.java     # Spring integration test
        ├── SpringTestServer.java
        ├── controller/UserController.java
        └── impl/UserServiceImpl.java
```

## Contributing

Issues and Pull Requests are welcome!

## License

[MIT License](LICENSE)

## Contact

If you have any questions or suggestions, feel free to contact via GitHub Issues.
