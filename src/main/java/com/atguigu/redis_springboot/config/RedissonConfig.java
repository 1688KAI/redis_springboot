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
     * 所有对Redisson的使用都是通过RedissonClient
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod="shutdown")
    public RedissonClient redissonClient() throws IOException {
        //1、创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://"+host+":6379")
                .setPassword(password)
                .setDatabase(database)
                .setTimeout(1000)
                .setRetryAttempts(3)
                .setRetryInterval(1000)
                .setPingConnectionInterval(1000)//**此项务必设置为redisson解决之前bug的timeout问题关键*****
                ;
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

}
