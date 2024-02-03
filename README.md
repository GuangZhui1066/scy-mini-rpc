# scy-mini-rpc

提供两种注册中心的实现：本地文件和 MySQL(支持分布式)。 </br>
支持 Provider 异步处理请求， Consumer 并发调用。 </br>

工程有五个 module </br>
● rpc-api：RPC 框架对外提供的服务接口    </br>
● rpc-netty：RPC 框架的实现            </br>
● service-api：RPC 服务的接口          </br>
● provider：PRC 服务的提供者           </br>
● consumer：PRC 服务的消费者           </br>