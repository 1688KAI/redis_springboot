package com.atguigu.redis_springboot.controller;

import org.redisson.Redisson;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class RedLockController {

    @Autowired
    private Redisson redisson;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 模拟商品超卖代码 <br>
     * 使用Redisson完成加锁、逻辑超时续期、释放锁等操作；<br>
     * RedLock
     *
     * @return
     */
    @RequestMapping("/deductStockRedLock")
    public String deductStockRedLock() {
        // 创建多个key，保存至redis
        String key1 = "lock1";
        String key2 = "lock2";
        String key3 = "lock3";
        //生成、获取锁
        RLock lock1 = redisson.getLock(key1);
        RLock lock2 = redisson.getLock(key2);
        RLock lock3 = redisson.getLock(key3);

        // 根据 多个lock对象 构建RedissonRedLock
        // 根据多个key构成的不同加锁对象，合并为一个 Redlock 对象
        RedissonRedLock redlock = new RedissonRedLock(lock1, lock2, lock3);

        try {
            /**
             * waitTime：尝试获取锁的最大等待时间，超过这个值则认为锁获取失败；<br>
             * leaseTime:锁的持有时间，超过这个时间，锁会直接失效(这个值应设置大于业务处理时间，确保有效时间内处理完业务)
             */
            boolean tryLock = redlock.tryLock(10, 30, TimeUnit.SECONDS);
            if (tryLock) {
                // 获取Redis数据库中的商品数量
                Integer stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
                // 减库存
                if (stock > 0) {
                    int realStock = stock - 1;
                    stringRedisTemplate.opsForValue().set("stock", String.valueOf(realStock));
                    System.out.println("商品扣减成功，剩余商品：" + realStock);
                } else {
                    System.out.println("库存不足.....");
                }
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            //释放锁
            redlock.unlock();
        }
        return "end";
    }
}
