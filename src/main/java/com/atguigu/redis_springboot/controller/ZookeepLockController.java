package com.atguigu.redis_springboot.controller;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/testZK")
public class ZookeepLockController {

    private static final Logger log = LoggerFactory.getLogger(ZookeepLockController.class);


    public static final String STOCK_KEY = "stock";
    public static final String LOCK_KEY = "lock";


    /**
     * 第一个版本 不控制并发情况
     * <p>
     * threadName =http-nio-8080-exec-73, stock=49
     * threadName =http-nio-8080-exec-30, stock=48
     * threadName =http-nio-8080-exec-13, stock=48
     * threadName =http-nio-8080-exec-24, stock=49
     * threadName =http-nio-8080-exec-83, stock=48
     * 多个线程获取同一个库存数量
     *
     * @return
     */
//    @ResponseBody
//    @GetMapping(value = "/lock/v1")
//    public String v1() {
//        String value = stringRedisTemplate.opsForValue().get(STOCK_KEY);
//        Integer stock = Integer.valueOf(value);
//        if (stock > 0) {
//            stringRedisTemplate.opsForValue().set(STOCK_KEY, String.valueOf(--stock));
//            System.out.println("threadName =" + Thread.currentThread().getName() + ", stock=" + stock);
//        }
//        return "hello";
//    }


}
