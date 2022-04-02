package com.atguigu.redis_springboot.controller;

import org.redisson.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/testRedis")
public class RedisLockController {

    private static final Logger log = LoggerFactory.getLogger(RedisLockController.class);


    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

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
    @ResponseBody
    @GetMapping(value = "/lock/v1")
    public String v1() {
        String value = stringRedisTemplate.opsForValue().get(STOCK_KEY);
        Integer stock = Integer.valueOf(value);
        if (stock > 0) {
            stringRedisTemplate.opsForValue().set(STOCK_KEY, String.valueOf(--stock));
            System.out.println("threadName =" + Thread.currentThread().getName() + ", stock=" + stock);
        }
        return "hello";
    }

    /**
     * 第二个版本 控制并发情况
     * 获取库存正常 解决版本一
     * <p>
     * 分布式情况下
     * nginx 负载-> 8080 8081
     * 问题: 分布式 synchronized 无法控制多个进程
     * threadName =http-nio-8081-exec-62, stock=1
     * threadName =http-nio-8080-exec-17, stock=1
     *
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/lock/v2")
    public String v2() {
        synchronized (this) {
            String value = stringRedisTemplate.opsForValue().get(STOCK_KEY);
            Integer stock = Integer.valueOf(value);
            if (stock > 0) {
                stringRedisTemplate.opsForValue().set(STOCK_KEY, String.valueOf(--stock));
                System.out.println("threadName =" + Thread.currentThread().getName() + ", stock=" + stock);
            }
        }
        return "hello";
    }

    /**
     * 第三版本 控制并发情况
     * 获取库存正常 解决版本二
     * <p>
     * 分布式情况下
     * nginx 80负载-> 8080 8081
     * <p>
     * 第二个版本 分布式情况下 控制并发
     * 分布式 synchronized 无法控制多个进程 解决版本二
     * <p>
     * <p>
     * 潜在的问题 删除key的时候 需要判断uuid是否一致 不是原子操作 可出现异常 导致key删除失败
     * 潜在的问题 由于业务复杂导致锁过期 其他服务获取锁 并获取同一库存数量 处理业务
     *
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/lock/v3")
    public String v3() {

        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        try {
            synchronized (this) {
                Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(LOCK_KEY, uuid, 30, TimeUnit.SECONDS);
                if (!aBoolean) {
                    return "no";
                }
                String value = stringRedisTemplate.opsForValue().get(STOCK_KEY);
                Integer stock = Integer.valueOf(value);
                if (stock > 0) {
                    stringRedisTemplate.opsForValue().set(STOCK_KEY, String.valueOf(--stock));
                    System.out.println("threadName =" + Thread.currentThread().getName() + ", stock=" + stock);
                }
            }
        } catch (Exception e) {
        } finally {
            if (uuid.equals(stringRedisTemplate.opsForValue().get(LOCK_KEY))) {
                stringRedisTemplate.delete(LOCK_KEY);
            }
        }

        return "hello";
    }

    /**
     * 第四版本 控制并发情况
     * 获取库存正常 解决版本三潜在问题
     *
     *
     *this.lockWatchdogTimeout = 30000L;
     *1）、锁的自动续期，如果业务超长，运行期间自动锁上新的30s。不用担心业务时间长，锁自动过期被删掉
     *2）、加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认会在30s内自动过期，不会产生死锁问题
     * myLock.lock(10,TimeUnit.SECONDS);   //10秒钟自动解锁,自动解锁时间一定要大于业务执行时间
     *问题：在锁时间到了以后，不会自动续期
     *1、如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是 我们制定的时间
     *2、如果我们指定锁的超时时间，就使用 lockWatchdogTimeout = 30 * 1000 【看门狗默认时间】
     *只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】,每隔10秒都会自动的再次续期，续成30秒
     * internalLockLeaseTime 【看门狗时间】 / 3， 10s
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/lock/v4")
    public String v4() {

        RLock myLock = redissonClient.getLock(LOCK_KEY);
        try {
            myLock.lock();
            String value = stringRedisTemplate.opsForValue().get(STOCK_KEY);
            Integer stock = Integer.valueOf(value);
            if (stock > 0) {
                stringRedisTemplate.opsForValue().set(STOCK_KEY, String.valueOf(--stock));
                System.out.println("threadName =" + Thread.currentThread().getName() + ", stock=" + stock);
            }
        } catch (Exception e) {
        } finally {
            myLock.unlock();
        }
        return "hello";
    }



    @ResponseBody
    @GetMapping(value = "/lock/v5")
    public String v5() {
        //1、获取一把锁，只要锁的名字一样，就是同一把锁
        RLock myLock = redissonClient.getLock("my-lock");

        //2、加锁
        myLock.lock();      //阻塞式等待。默认加的锁都是30s
        //this.lockWatchdogTimeout = 30000L;
        //1）、锁的自动续期，如果业务超长，运行期间自动锁上新的30s。不用担心业务时间长，锁自动过期被删掉
        //2）、加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认会在30s内自动过期，不会产生死锁问题
        // myLock.lock(10,TimeUnit.SECONDS);   //10秒钟自动解锁,自动解锁时间一定要大于业务执行时间
        //问题：在锁时间到了以后，不会自动续期
        //1、如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是 我们制定的时间
        //2、如果我们指定锁的超时时间，就使用 lockWatchdogTimeout = 30 * 1000 【看门狗默认时间】
        //只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】,每隔10秒都会自动的再次续期，续成30秒
        // internalLockLeaseTime 【看门狗时间】 / 3， 10s
        try {
            System.out.println("加锁成功，执行业务..." + Thread.currentThread().getId());
            try {
                TimeUnit.SECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            //3、解锁  假设解锁代码没有运行，Redisson会不会出现死锁
            System.out.println("释放锁..." + Thread.currentThread().getId());
            myLock.unlock();
        }

        return "hello";
    }
}
