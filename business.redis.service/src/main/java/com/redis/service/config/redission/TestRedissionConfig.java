package com.redis.service.config.redission;

public class TestRedissionConfig {

//    @Value("${spring.redis.host:127.0.0.1}")
//    private String host;
//
//    @Value("${spring.redis.port:6379}")
//    private String port;
//
//    @Value("${spring.redis.password}")
//    private String password;
//
//    @Value(value = "${spring.redisson.address}")
//    private String redissonAddress;
//
//    @Value(value = "${spring.redisson.password}")
//    private String redissonPassword;
//
//    @Value(value = "${spring.redisson.scanInterval}")
//    private int redissonScanInterval;
//
//    @Value(value = "${spring.redisson.retryAttempts}")
//    private int redissonRetryAttempts;
//
//    @Value(value = "${spring.redisson.timeout}")
//    private int redissonTimeout;

//    @Bean(destroyMethod = "shutdown")
//    public RedissonClient redisson(@Value("classpath:/redisson.yaml") Resource configFile) throws IOException {
//        Config config = Config.fromYAML(configFile.getInputStream());
//        return Redisson.create(config);
//    }

//    @Bean(destroyMethod = "shutdown")
//    public RedissonClient redissonClient() {
//        Config config = new Config();
//        SingleServerConfig singleServerConfig = config.useSingleServer().setAddress("redis://" + host + ":" + port);
//        if (StringUtils.isNotBlank(password)) {
//            singleServerConfig.setPassword(password);
//        }
//        System.out.println("------------- redisson -----------------------");
//        System.out.println(config.getTransportMode());
//        // TransportMode.EPOLL 只能在linux下使用,原则上应该比默认的nio快
//        // config.setTransportMode(TransportMode.EPOLL);
//        // transportMode（传输模式）默认值：TransportMode.NIO
//        return Redisson.create(config);
//    }


//       public RedissonClient getRedisClient() {
//           String[] nodes = redissonAddress.split(",");
//           for (int i = 0; i < nodes.length; i++) {
//               nodes[i] = "redis://" + nodes[i];
//           }
//           Config config = new Config();
//           System.out.println("------------- redisson -----------------------");
//           System.out.println(config.getTransportMode());
//           config.useClusterServers() //这是用的集群server
//                   .setScanInterval(redissonScanInterval) //设置集群状态扫描时间
//                   .addNodeAddress(nodes).setRetryAttempts(redissonRetryAttempts).setTimeout(redissonTimeout);
//           if (StringUtils.isNotEmpty(redissonPassword)) {
//               config.useClusterServers().setPassword(redissonPassword);
//           }
//           return Redisson.create(config);
//       }

//    RedissonClient redissonSingle() {
//        Config config = new Config();
//        String node = redisProperties.getSingle().getAddress();
//        node = node.startsWith("redis://") ? node : "redis://" + node;
//        SingleServerConfig serverConfig = config.useSingleServer()
//                .setAddress(node)
//                .setTimeout(redisProperties.getPool().getConnTimeout())
//                .setConnectionPoolSize(redisProperties.getPool().getSize())
//                .setConnectionMinimumIdleSize(redisProperties.getPool().getMinIdle());
//        if (StringUtils.isNotBlank(redisProperties.getPassword())) {
//            serverConfig.setPassword(redisProperties.getPassword());
//        }
//        return Redisson.create(config);
//    }
}
