package com.atguigu.redis_springboot.config;

import org.apache.zookeeper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

/**
 * Zookeepr实现分布式锁
 */
@Service
public class ZkLockUtil {
    private String lockNameSpace = "/mylock";

    private String nodeString = lockNameSpace + "/test1";

    private Lock mainLock;

    @Autowired
    private ZooKeeper zk;



    private void ensureRootPath() throws InterruptedException {
        try {
            if (zk.exists(lockNameSpace,true)==null){
                zk.create(lockNameSpace,"".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    private void watchNode(String nodeString, final Thread thread) throws InterruptedException {
        try {
            zk.exists(nodeString, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    System.out.println( "==" + watchedEvent.toString());
                    if(watchedEvent.getType() == Event.EventType.NodeDeleted){
                        System.out.println("Threre is a Thread released Lock==============");
                        thread.interrupt();
                    }
                    try {
                        zk.exists(nodeString,new Watcher() {
                            @Override
                            public void process(WatchedEvent watchedEvent) {
                                System.out.println( "==" + watchedEvent.toString());
                                if(watchedEvent.getType() == Event.EventType.NodeDeleted){
                                    System.out.println("Threre is a Thread released Lock==============");
                                    thread.interrupt();
                                }
                                try {
                                    zk.exists(nodeString,true);
                                } catch (KeeperException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        });
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            });
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取锁
     * @return
     * @throws InterruptedException
     */
    public boolean lock() throws InterruptedException {
        String path = null;
        ensureRootPath();
        watchNode(nodeString,Thread.currentThread());
        while (true) {
            try {
                path = zk.create(nodeString, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            } catch (KeeperException e) {
                System.out.println(Thread.currentThread().getName() + "  getting Lock but can not get");
                try {
                    Thread.sleep(5000);
                }catch (InterruptedException ex){
                    System.out.println("thread is notify");
                }
            }
            if (!path.trim().isEmpty()) {
                System.out.println(Thread.currentThread().getName() + "  get Lock...");
                return true;
            }
        }
    }

    /**
     * 释放锁
     */
    public void unlock(){
        try {
            zk.delete(nodeString,-1);
            System.out.println("Thread.currentThread().getName() +  release Lock...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}
