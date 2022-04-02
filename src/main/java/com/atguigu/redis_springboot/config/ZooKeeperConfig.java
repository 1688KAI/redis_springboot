package com.atguigu.redis_springboot.config;


import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@Configuration
public class ZooKeeperConfig {

    @Value("${zookeeper.connectString}")
    private String zkQurom;

    private CountDownLatch connectedSignal = new CountDownLatch(1);

    @Bean("zooKeeper")
    public ZooKeeper zooKeeperClient() throws IOException {

        return new ZooKeeper(zkQurom, 6000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println("Receive event " + watchedEvent);
                if (Event.KeeperState.SyncConnected == watchedEvent.getState()){
                    System.out.println("connection is established...");
                }
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    connectedSignal.countDown();
                }
            }
        });
    }
}
