package com.atguigu.redis_springboot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.micrometer.core.instrument.util.JsonUtils;
import jodd.time.TimeUtil;
import lombok.SneakyThrows;
import org.apache.zookeeper.*;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class RedisSpringbootApplicationTests {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    ZooKeeper zooKeeper;

    @Test
    void contextLoads() {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter("sample");

        bloomFilter.tryInit(55000000L, 0.03);

        System.out.println(bloomFilter.add("222"));
        System.out.println(bloomFilter.add("set-k1"));
        System.out.println(bloomFilter.add("set-k2"));
        System.out.println(bloomFilter.add("set-k3"));
    }

    /**
     * zk 同步监听
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    void monitorConfiguration() throws KeeperException, InterruptedException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        Map map = new HashMap();
        map.put("key", "lastTime");
        map.put("value", "2022-4-3 08:34:49");
        byte[] bytes = objectMapper.writeValueAsBytes(map);

        String s = zooKeeper.create("/config", bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);


        System.out.println("s = " + s);

        //创建监听器
        Watcher watcher = new Watcher() {
            @SneakyThrows
            @Override
            public void process(WatchedEvent event) {
                //节点发生改变
                if (event.getType() == Event.EventType.NodeDataChanged
                        && event.getPath().equals("/config")) {
                    System.out.println("event path = " + event.getPath() + "发生改变");
                    //再次监听
                    byte[] data = zooKeeper.getData("/config", this, null);
                    Map map1 = objectMapper.readValue(data, Map.class);
                    System.out.println("Watched /config = " + map1);
                }
            }
        };

        //监听
        byte[] data = zooKeeper.getData("/config", watcher, null);
        Map map1 = objectMapper.readValue(data, Map.class);
        System.out.println("get /config = " + map1);

        Thread.sleep(Long.MAX_VALUE);

    }

}
