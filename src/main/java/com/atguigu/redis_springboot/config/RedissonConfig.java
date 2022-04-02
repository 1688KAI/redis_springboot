package com.atguigu.redis_springboot.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @Value("${spring.redis.port:6379}")
    String port;

    @Value("${spring.redis.password:7347}")
    String password;

    @Value("${spring.redis.database:6}")
    Integer database;

    @Value("${spring.redis.sentinel.nodes}")
    List<String> sentinelNodes;

    @Value("${spring.redis.cluster.nodes}")
    List<String> clusterNodes;
    /**
     * Redisson单机配置
     *
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "spring.redis.mode", havingValue = "single")
    public RedissonClient SetRedissonClientStandAlone() throws IOException {
        //1、创建配置
        Config config = new Config();
        config.useSingleServer().setAddress(setPrefix(host + port))
                .setDatabase(database)
                .setTimeout(1000)
                .setRetryAttempts(3)
                .setRetryInterval(1000)
                .setPingConnectionInterval(1000)//**此项务必设置为redisson解决之前bug的timeout问题关键*****
        ;
        RedissonClient redissonClient = Redisson.create(config);
        System.out.println("redissonClient = 单机模式 ");
        return redissonClient;
    }

    /**
     * 设置前缀
     * @param node
     * @return
     */
    private String setPrefix(String node) {
        return "redis://" + node;
    }


    /**
     * redisson 哨兵配置
     *
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "spring.redis.mode", havingValue = "sentinal")
    public RedissonClient setRedissonClientSentinal() throws IOException {
        //1、创建配置
        Config config = new Config();
        List<String> collect = sentinelNodes.stream().map(this::setPrefix).collect(Collectors.toList());

        config.useSentinelServers().setMasterName("mymaster")
                //可以用"rediss://"来启用SSL连接
                .setDatabase(database)
                .setTimeout(1000)
                .setRetryAttempts(3)
                .setRetryInterval(1000)
                .setPingConnectionInterval(1000)//**此项务必设置为redisson解决之前bug的timeout问题关键*****
                .setSentinelAddresses(collect)
        ;
        RedissonClient redissonClient = Redisson.create(config);
        System.out.println("redissonClient = 哨兵模式 ");
        return redissonClient;
    }


    /**
     * redisson 集群配置
     *
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "spring.redis.mode", havingValue = "cluster")
    public RedissonClient setRedissonClientCluster() throws IOException {
        List<String> collect = clusterNodes.stream().map(this::setPrefix).collect(Collectors.toList());

        //1、创建配置
        Config config = new Config();
        config.useClusterServers()
                //可以用"rediss://"来启用SSL连接
                .setScanInterval(2000) // 集群状态扫描间隔时间，单位是毫秒
                .setTimeout(1000)
                .setRetryAttempts(3)
                .setRetryInterval(1000)
                .setPingConnectionInterval(1000)//**此项务必设置为redisson解决之前bug的timeout问题关键*****
                .setNodeAddresses(collect)
        ;
        RedissonClient redissonClient = Redisson.create(config);
        System.out.println("redissonClient = 集群模式 ");
        return redissonClient;

    }


}
