package com.atguigu.redis_springboot.controller;

import com.atguigu.redis_springboot.service.SecKill_redisByScript;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Random;

/**
 * @author kai
 * @date 2021/5/12 12:09
 * @description:
 */
@RestController
@RequestMapping("/test")
public class SecKillController {

    @PostMapping()
    public Boolean doseckill(@RequestParam String prodid) throws IOException {

        String userid = new Random().nextInt(50000) +"" ;
        //boolean isSuccess=SecKill_redis.doSecKill(userid,prodid);
        boolean isSuccess= SecKill_redisByScript.doSecKill(prodid,prodid);
        return isSuccess;
    }

}
