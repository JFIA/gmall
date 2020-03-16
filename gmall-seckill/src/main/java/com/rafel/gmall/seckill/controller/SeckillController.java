package com.rafel.gmall.seckill.controller;


import com.rafel.gmall.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;


    /***
     * 先到先得式秒杀
     * @return
     */
    @RequestMapping("secKill")
    @ResponseBody
    public String secKill(){

        Jedis jedis = redisUtil.getJedis();

        int stock = Integer.parseInt(jedis.get("103"));

        RSemaphore semaphore = redissonClient.getSemaphore("103");
        boolean acquire = semaphore.tryAcquire();

        if (acquire){

            System.out.println("success," + "库存数量" + stock);
            // 消息队列发送订单通知
        } else {
            System.out.println("fail");
        }

        jedis.close();

        return null;
    }

    /***
     * 拼手气秒杀
     * @return
     */
    @RequestMapping("kill")
    @ResponseBody
    public String kill() {
        // 开启商品的监控
        Jedis jedis = redisUtil.getJedis();
        jedis.watch("103");
        int stock = Integer.parseInt(jedis.get("103"));

        if (stock > 0) {

            Transaction multi = jedis.multi();// 开启事物
            multi.incrBy("103", -1);
            List<Object> exec = multi.exec();

            if (exec != null && exec.size() > 0) {
                System.out.println("success," + "库存数量" + stock);
                // 消息队列发送订单通知
            } else {
                System.out.println("fail");
            }

        }

        jedis.close();

        return null;
    }
}
