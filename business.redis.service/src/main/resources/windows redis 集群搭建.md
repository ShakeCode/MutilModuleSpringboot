[toc]

# windows redis 集群搭建

## 一. 准备软件环境

1. windows-redis 5.0.10版本
2. windows10
3. ruby 安装包(针对Redis 版本 3 或 4)
4. 官网地址: https://redis.io/topics/cluster-tutorial#creating-the-cluster
5. redis 集群分布式特性: https://redis.io/topics/cluster-spec
6. 请注意，按预期工作的**最小集群**需要包含至少三个主节点。对于您的第一次测试，强烈建议启动一个具有三个主节点和三个从节点的六节点集群。

## 二. 创建三主三从集群

1. 摘取官网的版本区别

   - 如果您使用 Redis 5 或更高版本，这很容易实现，因为我们在 Redis Cluster 命令行实用程序的帮助下嵌入到 中`redis-cli`，可用于创建新集群、检查或重新分片现有集群等。

     对于 Redis 版本 3 或 4，有一个`redis-trib.rb`非常相似的旧工具。您可以`src`在Redis源代码分发目录中找到它。您需要安装`redis`gem 才能运行`redis-trib`.

   -   Redis Cluster 在5.0之后取消了ruby脚本 **redis-trib.rb**的支持（手动命令行添加集群的方式不变），集合到redis-cli里，避免了再安装ruby的相关环境。直接使用redis-clit的参数--cluster 来取代

   - **主节点可读可写，从节点只读不能写**

   - 涉及命令

   - ![image-20210605165624746](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605165624746.png)

2. 开始搭建

   1. 官网建议3个主节点，3个从节点，高可用性更稳定

   2. 创建目录win-redis-cluster，再创建节点文件夹分别命名 7000, 7001, 7002 ，7003，7004，7005将redis安装包内容复制到6个文件夹

   3. 修改各节点的配置文件redis.windows.conf (cluster-config-file 需按照端口区分文件名)

   4. 设置主从节点，这里选择7000为主节点，7001,7002为从节点

      1. 不设置主从节点, 由执行搭建集群命令后自行分配

         ```
         redis-cli --cluster create 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005 --cluster-replicas 1
         ```

         

      1. 打开每个节点的配置文件搜索并设置

         ```java
         port 7000
         cluster-enabled yes
         cluster-config-file nodes-7000.conf
         cluster-node-timeout 5000
         appendonly yes
         ```

         其中cluster-config-file 需要安装端口区分命名文件名!!! cluster-enabled 必须是yes !!!

   5. 执行各节点服务启动命令

      - 在各文件夹执行cmd

      - **redis-server.exe redis.windows.conf**
      - 生成文件log.txt，nodes-7002.conf(存储集群节点信息)

   6. 注意

      **在启动集群的时候redis数据库必须是空的，否则会报错提示：**[ERR] Node 127.0.0.1:7001 is not empty. Either the node already knows other nodes (check with CLUSTER NODES) or contains some key in database 0。

      解决方法：删除生成的配置文件nodes.conf，如果不行则说明现在创建的结点包括了旧集群的结点信息，需要删除redis的持久化文件后再重启redis，比如：appendonly.aof、dump.rdb。

   7. 修改日志输入到控制台

      - Redis默认的设置为verbose，开发测试阶段可以用debug，生产模式一般选用notice

        \1. debug：会打印出很多信息，适用于开发和测试阶段

        \2. verbose（冗长的）：包含很多不太有用的信息，但比debug要清爽一些

        \3. notice：适用于生产模式

        \4. warning : 警告信息

      - 打开配置文件，loglevel notice 改为 loglevel debug

      - logfile log.txt 改为 logfile ""  

      - 重启启动后打印日志在控制台

        ![image-20210605172055470](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605172055470.png)

   8.  创建集群主从节点,设置主节点1个副本， 如果需要集群需要认证，则在最后加入 **-a xx** 即可

      ```ruby
      redis-cli --cluster create 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005 --cluster-replicas 1
      ```

      ![image-20210605194939135](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605194939135.png)

   9.   使用客户端连接6个节点测试

         key 命名遵循阿里redis使用规范使其产生文件夹区分其他vapp

      ![image-20210605201050242](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605201050242.png)

      ![image-20210605214818040](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605214818040.png)

      1. 发现只有一个0号数据库，默认是16个数据库的

      2. 在主节点或者从节点 进入控制台，设置 HSET promotion:black phone 123 后 都可以在其他节点同步到数据

      3. 这里显示有问题，在其他节点控制台设置索引，就可以看到起表被加载出来了

      4. 新增7007作为master节点

         - 赋值文件夹7007

         - ```ruby
           ./redis-cli --cluster add-node 127.0.0.1:7007  127.0.0.1:7001
           ```

         - 检查节点信息发现没有分配slots

           ```ruby
           ./redis-cli --cluster check 127.0.0.1:7001
           ```

         - 重新分配slots

           ```ruby
           ./redis-cli --cluster reshard 127.0.0.1:7007
           ```

      5. 新增7008从节点到7007

         - ```ruby
           ./redis-cli --cluster add-node 127.0.0.1:7008 127.0.0.1:7007 --cluster-slave --cluster-master-id e3ed175cd38c9ea5b7a0827f2be7b8bfa9385ba2
           ```

      6. 删除从节点7008

         - ```ruby
           ./redis-cli --cluster del-node 127.0.0.1:7008 609e99ae01ce067323f8c44207f512b5cd3546e2
           ```

      7. 删除主节点7007

         1. 先转移slots

            ```ruby
            ./redis-cli --cluster reshard 127.0.0.1:7001
            ```

         2. 分配均匀使用

            ```ruby
            ./redis-cli --cluster rebalance --cluster-threshold 127.0.0.1:7001
            ```

         3. 删除7007主节点

            ```ruby
            ./redis-cli --cluster del-node 127.0.0.1:7007 `73f19b384906113507b25f256a781ce184777162
            ```

      8. 测试从节点删除后从节点作为主节点

         ![image-20210605222036075](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605222036075.png)

         - 获取7004端口的进程

           1. 执行命令: netstat -ano | findstr 7004

           ![image-20210605222158689](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605222158689.png)

         - 获取5532进程的进程任务

           1. 执行命令: tasklist|findstr 5532

              ![image-20210605222340456](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605222340456.png)

         - 删除5532进程- 结束进程： TASKKILL /F /IM 进程名称 /T 或 TASKKILL /PID 进程ID /T

           1. 执行命令:  TASKKILL /F /PID 5532

              ![image-20210605222605446](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605222605446.png)

      9. 查看节点情况发现7004节点不见了，而7000还是作为主节点

         ![image-20210605222956447](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605222956447.png)

      10. 重新运行7004节点后新增7004节点到主节点7000作为从节点

          ![image-20210605223313725](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605223313725.png)

      11. 删除7000，7004主节点，发现其从节点会作为主节点继续值守

          ![image-20210605223852441](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605223852441.png)

      12. 再删除7002，7005从节点，发现只剩下一对主从节点了

          ![image-20210605224158287](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605224158287.png)

          ​     

      13. 删除最后一个主节点

          1. 执行命令: redis-cli --cluster del-node 127.0.0.1:7001 4 4aa12aca6fb13810f0262ac754942a686b30f46d

          2. 发现删除失败

             ![image-20210605224850697](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605224850697.png)

                      8. 其他集群命令参考

      1. https://www.cnblogs.com/zhoujinyi/p/11606935.html
         2. https://blog.csdn.net/qq_28289405/article/details/84063921?utm_medium=distribute.pc_relevant.none-task-blog-baidujs_title-1&spm=1001.2101.3001.4242
         3. https://www.choupangxia.com/2019/11/07/redis-node-is-not-empty/
         4. https://www.cnblogs.com/tanghaorong/p/14339880.html

## 二. 创建一主二从集群

1. 将单机redis程序包复制成3分: 7001,7002,7003 （其中7001作为主节点，7002，7003 作为从节点）

2. 修改配置文件redis.windows.conf

   |                      | port | \# slaveof <masterip> <masterport> | cluster-enabled |
   | -------------------- | ---- | ---------------------------------- | --------------- |
   | Master主节点  7001   | 7001 | 不设置                             | yes             |
   | Slaver1 从节点  7002 | 7002 | slaveof 127.0.0.1 7001             | no              |
   | Slaver1  从节点7003  | 7003 | slaveof 127.0.0.1 7001             | no              |

3. 先启动主节点7001，后启动2个从节点

4. 主节点支持读写，从节点只支持读取，主节点宕掉，从节点还是只是可读，不可写，也不会自动升级为主节点

5. 连接主节点后查看集群关系

   ![image-20210605230809214](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605230809214.png)

![image-20210605230833489](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605230833489.png)

6. 发现主节7001点挂了后，从节点7002或7003没有选举新的主节点上任，由此需要加上哨兵机制

   ![image-20210605231554538](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605231554538.png)

   ##  

## 三. 搭建一主二从三哨兵机制

0. ### 基于搭建好一主二从集群

1. ### 常用命令

   1. ```json
      1. 根据配置文件启动redis：./src/redis-server redis.conf
      2. 启动哨兵在src下 ./redis-sentnel ../sentnel.conf          要是启动失败后边加上两个横杠 **--**
      3. 启动redis客户端：redis-cli -p port
      4. Slaver连接Master：slaveof host：ip    
      5. 关闭Redis：shutdown
      6. 查看主从信息：info Replication
      ```

2. 在主目录下分别创建sentinel.conf文件

   1. 主节点sentinel.conf

      ```ruby
      port 27001
      sentinel monitor zlj-master 127.0.0.1 7001 2
      sentinel down-after-milliseconds zlj-master 5000
      sentinel parallel-syncs zlj-master 1
      sentinel failover-timeout zlj-master 15000
      ```

      

   2. 从节点sentinel7002.conf

      ```ruby
      port 27002
      sentinel monitor zlj-master 127.0.0.1 7002 2
      sentinel down-after-milliseconds zlj-master 5000
      sentinel parallel-syncs zlj-master 1
      sentinel failover-timeout zlj-master 15000
      ```

   3. 从节点sentinel7003.conf

      ```ruby
      port 27003
      sentinel monitor zlj-master 127.0.0.1 7003 2
      sentinel down-after-milliseconds zlj-master 5000
      sentinel parallel-syncs zlj-master 1
      sentinel failover-timeout zlj-master 15000
      ```

   4. 文件效果

      ![image-20210606001159132](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210606001159132.png)

   5. 参数解释

      ```ruby
      1. port :当前Sentinel服务运行的端口(和其redis tcp接口一一映射)
      2.sentinel monitor mymaster 127.0.0.1 6379 2:Sentinel去监视一个名为mymaster的主redis实例，这个主实例的IP地址为本机地址127.0.0.1，端口号为6379，而将这个主实例判断为失效至少需要2个 Sentinel进程的同意，只要同意Sentinel的数量不达标，自动failover就不会执行
      3.sentinel down-after-milliseconds mymaster 5000:指定了Sentinel认为Redis实例已经失效所需的毫秒数。当 实例超过该时间没有返回PING，或者直接返回错误，那么Sentinel将这个实例标记为主观下线。只有一个 Sentinel进程将实例标记为主观下线并不一定会引起实例的自动故障迁移：只有在足够数量的Sentinel都将一个实例标记为主观下线之后，实例才会被标记为客观下线，这时自动故障迁移才会执行
      4.sentinel parallel-syncs mymaster 1：指定了在执行故障转移时，最多可以有多少个从Redis实例在同步新的主实例，在从Redis实例较多的情况下这个数字越小，同步的时间越长，完成故障转移所需的时间就越长
      5.sentinel failover-timeout mymaster 15000：如果在该时间（ms）内未能完成failover操作，则认为该failover失败
      ```

3. 先启动主节点，后从节点redis服务

   1. 分别在目录下执行: redis-server.exe redis.windows.conf

4. 先启动主节点的哨兵，后启动从节点的哨兵服务

   1. 分别在目录下执行命令
      - redis-server.exe sentinel.conf --sentinel
      - redis-server.exe sentinel7002.conf --sentinel
      - redis-server.exe sentinel7003.conf --sentinel

5. 启动效果

   ![image-20210605235854792](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210605235854792.png)

6. 查看哨兵状态

   1. 运行命令连接哨兵端口

      - redis-cli.exe -h 127.0.0.1 -p 27002

   2. 查看效果

      ![image-20210606000154803](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210606000154803.png)

7. 验证主节点宕机后，在其中一个从节点选举出新的主节点

   - 关闭主节点7001的服务

   - 可看到主节点转移到7002服务

     ![image-20210606000800352](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210606000800352.png)

   - 可以看到7002服务以及上升到master主节点，剩下的7003是从服务，由此组成一主一从服务，实现高可用

     ![image-20210606001100663](C:\Users\lijun\AppData\Roaming\Typora\typora-user-images\image-20210606001100663.png)

8. 总结

   1. Master可读可写，Slaver只能读，不能写
   2. Master可以对应多个Slaver，但是数量越多压力越大，延迟就可能越严重
   3. Master写入后立即返回，几乎同时将写入异步同步到各个Slaver，所以基本上延迟可以忽略
   4. 可以通过slaveof no one命令将Slaver升级为Master（当Master挂掉时，手动将某个Slaver变为Master）
   5. 可以通过sentinel哨兵模式监控Master，当Master挂掉时自动选举Slaver变为Master，其它Slaver自动重连新的Master

9. 参考资料

   1. https://www.cnblogs.com/justdoyou/p/10253668.html
   2. https://redis.io/topics/sentinel
   3. https://blog.csdn.net/qq_36881887/article/details/81262425?utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromMachineLearnPai2%7Edefault-2.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromMachineLearnPai2%7Edefault-2.control

