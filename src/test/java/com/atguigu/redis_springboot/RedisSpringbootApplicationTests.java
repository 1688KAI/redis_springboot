package com.atguigu.redis_springboot;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RedisSpringbootApplicationTests {

    @Autowired
    RedissonClient redissonClient;

    @Test
    void contextLoads() {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter("sample");

        bloomFilter.tryInit(55000000L, 0.03);

        System.out.println(bloomFilter.add("222"));
        System.out.println(bloomFilter.add("set-k1"));
        System.out.println(bloomFilter.add("set-k2"));
        System.out.println(bloomFilter.add("set-k3"));
    }

}
