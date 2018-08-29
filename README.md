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
### jmeter压测

- 压测商品列表接口`/goods/to_list`

1000个线程循环压测10次后的结果：

做页面优化前：

![](https://ws1.sinaimg.cn/large/73d640f7ly1fuqmauva7gj214d0663zh.jpg)

做页面缓存优化后：

![](https://ws1.sinaimg.cn/large/73d640f7ly1fuqmbwd33rj214h06575a.jpg)

- 压测秒杀接口`/seckill/do_seckill`

这边需要通过UserUtil工具类生成多个用户token，然后通过jmeter配置元件->csv数据文件设置导入token文件

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

## 页面优化

### 页面缓存和对象缓存
1. 商品列表页和详情页静态资源缓存在redis中，过期时间为一分钟，每次先去redis里面取，redis中查不到再去mysql访问，并更新缓存
2. 动态资源通过前端js的ajax动态加载
3. SeckillUserService和OrderService根据id查询的service中对user对象和订单详情对象进行了对象缓存，这样不用每次去访问mysql查询，直接先查询缓存即可，
更新的缓存的时候需要注意先后顺序，一定是先更新数据库再更新缓存。


### 防止超卖
1. 更新库存在库存量大于0时才更新

```sql
update seckill_goods set stock_count=stock_count-1 where goods_id=#{goodsId} and stock_count > 0
```

2. 防止一个用户同事秒杀一个商品两次，为seckill_order表的user_id和goods_id添加联合btree索引

```sql
alter table seckill_order add unique key(user_id,goods_id);
```

3. 多个用户同时进行秒杀操作，同时判断库存不为0，然后均写入写入订单，出现下订单详情异常需要在`SeckillService`中加入如下判断：

```java
//减库存 下订单 写入秒杀订单
    boolean success = goodsService.reduceStock(goods);
    
    if (success) {
        return orderService.createOrder(user, goods);
    }
```

