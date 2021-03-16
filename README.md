# 介绍
RuntimeAgent是一个基于Spring、Zookeeper的Java类库，提供服务发现、实例注册、可扩展的事件插件机制。
设计分为管理端（RTAManagerApp）和运行时端(RTARuntimeApp)，运行时端设计为多端部署的承载服务的运行时，管理端设计则为管理监控多个运行时端的管理器。
插件机制可以方便的扩展业务逻辑。
>编译：詹德欣（zhandx@163.com）
>版本：1.0.0-SNAPSHOT - 2021年3月

--------------------------------------
### 内容
[RTABootApps](#RTABootApps)
 -  [RuntimeAgent](#RuntimeAgent)
 -  [RTARuntimeApp](#RTARuntimeApp)
 -  [RTAManagerApp](#RTAManagerApp)
 -  [RuntimeAgentUse101tec](#RuntimeAgentUse101tec)

--------------------------------------
## RTABootApps
RuntimeAgent根pom工程
## RuntimeAgent
RuntimeAgent公共类库，主要逻辑在此工程
## RTARuntimeApp
RuntimeAgentApp是运行时样例工程，启动时自动注册自己和承载服务，以供RTAManagerApp监控，同时监听RTAEvent事件以接收RTAManagerApp的推送事件。
## RTAManagerApp
RTAManagerApp是管理端样例工程，启动时监控运行时端的自我注册。接收和推送事件到运行时。
## RuntimeAgentUse101tec
使用101tec的但工程开发版本，停滞了，不在继续开发，改用Curator组件

--------------------------------------
## 配置
```
rta:
  app: nms                      #应用名称（全局唯一）
  type: runtime                 # runtime or manager
  pid: rta-runtime-01           #进程id（jvm进程id，在应用内唯一）
  address: 127.0.0.1            #部署IP地址（物理机、虚机、k8s节点IP地址）
  port: 18086                   #部署端口（物理机和虚机进程启动端口，k8s代理容器端口）
  heartDelay: 10                #心跳间隔时间（单位秒）
  retry: 10                     #ZkClient重试次数
  retryDuration: 5000           #ZkClient重试间隔事件，单位毫秒
  zkAddress: 127.0.0.1:2181
  zkSessionTimeout: 30000
  zkConnectionTimeout: 30000
  meta:
    k1: v1
```
---------------------------------------
## 插件扩展
1. 自定义事件，继承AbstractRTAEvent
2. 自定义事件插件，继承AbstractRTAEventPlugin
3. 收发事件

## ZK存储结构
```
/rta/{app}/event/up             #公共上行事件订阅点，管理端和运行时端都会用到，但具有相对意义        
/rta/{app}/event/down           #公共下行事件订阅点，管理端和运行时端都会用到，但具有相对意义
/rta/{app}/rvm/{pid}/eventup    #运行时端上行事件订阅点和公共上行事件订阅点互补
/rta/{app}/rvm/{pid}/eventdown  #运行时端下行事件订阅点和公共上行事件订阅点互补
/rta/{app}/service              #服务发现订阅点，用于服务发现和路由
```
## 更新内容