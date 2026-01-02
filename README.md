light-rpc (Parent, pom)

rpc-api (存放接口和 DTO，最底层)

rpc-common (存放工具类、枚举、常量)

rpc-core (核心，依赖 common 和 api)

rpc-registry (注册中心，依赖 core)

rpc-test (测试 demo)