## 登录模块
1. 两次md5校验保证无法通过彩虹表倒反推出用户密码

    两次md5
    - 用户端：pass = md5 (明文 + 固定salt)
    - 服务端：pass = md5 (用户输入 + 随机salt)

    导入依赖
    ```xml
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>3.6</version>
    </dependency>
    
    <dependency>
        <groupId>commons-codec</groupId>
        <artifactId>commons-codec</artifactId>
    </dependency>
    
    ```

2. JSR303参数校验

    @NotNull 验证是否为空
    
    @Length(min=32) 验证长度是否为32
 
    自定义验证器@IsMobile实现ConstraintValidator接口（通过正则表达式验证手机号是否合法）
    
    ```xml
       <!--自定义参数校验器+全局异常处理器-->
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-validation</artifactId>
       </dependency>
    ```

3. 全局异常处理器
   
   定义全局异常GlobalException拦截异常，继承自RuntimeException

4. 分布式Session

    - 在分布式集群的条件上，用户的Session存储和同步是个问题。
    - 本项目采用redis对token进行缓存，这里key为`prefix + token`，value为user对象
    - 客户端每次访问服务款携带Cookie，服务端可以通过客户端Cookie的token获取Session    
    - Cookie的过期时间以用户最后一次登录为准，每登录一次修改过期时间，默认过期时间是两天
    - 通过`UserArgumentResolver`的回调功能将Controller需要的参数注入，减少冗余代码
    
## 压测

### redis压测(使用redis-benchmark)
```bash
redis-benchmark -h 127.0.0.1 -p 6379 -c 100 -n 100000
```
100个并发连接 10万个请求

可以达到10万多QPS

![](https://ws1.sinaimg.cn/large/73d640f7ly1fuoqd1x9djj20dx0d4q4d.jpg)

```bash
redis-benchmark -h 127.0.0.1 -p 6379 -q -d 100
```
存取大小为100字节的数据包

![](https://ws1.sinaimg.cn/large/73d640f7ly1fuoqjm0jqpj20k40cj77e.jpg)

```bash
redis-benchmark -t set,lpush -n 100000 -q
```
只测试某些操作的性能

![](https://ws1.sinaimg.cn/large/73d640f7ly1fuoqn2n414j20b901h74e.jpg)


```bash
redis-benchmark -n 100000 -q script load "redis.call('set','foo','bar')"
```
只测试某些数值存取的性能

![](https://ws1.sinaimg.cn/large/73d640f7ly1fuoqpz3cikj20md00naa3.jpg)
