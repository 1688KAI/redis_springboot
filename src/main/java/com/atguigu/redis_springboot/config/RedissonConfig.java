package com.atguigu.redis_springboot.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: kai
 * @createTime: 2020-06-11 09:39
 **/
@Configuration
public class RedissonConfig {


    @Value("${spring.redis.host:47.99.130.101}")
    String host;


    @Value("${spring.redis.password:7347}")
    String password;


    @Value("${spring.redis.database:6}")
    Integer database;
    /**
     * Redisson单机配置
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod="shutdown")
    public RedissonClient SetRedissonClientStandAlone() throws IOException {
        //1、创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379")
                .setDatabase(database)
                .setTimeout(1000)
                .setRetryAttempts(3)
                .setRetryInterval(1000)
                .setPingConnectionInterval(1000)//**此项务必设置为redisson解决之前bug的timeout问题关键*****
                ;
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }


//    /**
//     * redisson 哨兵配置
//     * @return
//     * @throws IOException
//     */
//    @Bean(destroyMethod="shutdown")
//    public RedissonClient setRedissonClientSentinal() throws IOException {
//        //1、创建配置
//        Config config = new Config();
//        config.useSentinelServers().setMasterName("mymaster")
//                //可以用"rediss://"来启用SSL连接
//                .addSentinelAddress("redis://192.168.157.130:26379", "redis://192.168.157.130:26380")
//                .addSentinelAddress("redis://192.168.157.130:26381")
//                .setDatabase(database)
//                .setTimeout(1000)
//                .setRetryAttempts(3)
//                .setRetryInterval(1000)
//                .setPingConnectionInterval(1000)//**此项务必设置为redisson解决之前bug的timeout问题关键*****
//        ;
//        RedissonClient redissonClient = Redisson.create(config);
//        return redissonClient;
//    }
//
//
//    /**
//     * redisson 集群配置
//     * @return
//     * @throws IOException
//     */
//    @Bean(destroyMethod="shutdown")
//    public RedissonClient setRedissonClientCluster() throws IOException {
//        //1、创建配置
//        Config config = new Config();
//        config.useClusterServers()
//                //可以用"rediss://"来启用SSL连接
//                .setScanInterval(2000) // 集群状态扫描间隔时间，单位是毫秒
//                //可以用"rediss://"来启用SSL连接  配置主节点
//                .addNodeAddress("redis://127.0.0.1:7000", "redis://127.0.0.1:7001")
//                .addNodeAddress("redis://127.0.0.1:7002")
//                .setTimeout(1000)
//                .setRetryAttempts(3)
//                .setRetryInterval(1000)
//                .setPingConnectionInterval(1000)//**此项务必设置为redisson解决之前bug的timeout问题关键*****
//        ;
//        RedissonClient redissonClient = Redisson.create(config);
//        return redissonClient;
//    }


}
