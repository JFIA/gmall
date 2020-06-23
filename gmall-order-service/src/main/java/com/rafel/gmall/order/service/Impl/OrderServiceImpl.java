package com.rafel.gmall.order.service.Impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.rafel.gmall.bean.OmsOrder;
import com.rafel.gmall.bean.OmsOrderItem;
import com.rafel.gmall.mq.ActiveMQUtil;
import com.rafel.gmall.order.mapper.OmsOrderItemMapper;
import com.rafel.gmall.order.mapper.OmsOrderMapper;
import com.rafel.gmall.service.CartService;
import com.rafel.gmall.service.OrderService;
import com.rafel.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.List;
import java.util.UUID;


@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Reference
    CartService cartService;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public String genTradeCode(String memberId) {

        Jedis jedis = redisUtil.getJedis();

        String tradeKey = "user:" + memberId + ":tradeCode";

        String tradeCode = UUID.randomUUID().toString();

        jedis.setex(tradeKey, 60 * 15, tradeCode);

        jedis.close();

        return tradeCode;
    }

    @Override
    public boolean checkTradeCode(String memberId, String tradeCode) {

        Jedis jedis = redisUtil.getJedis();

        String tradeKey = "user:" + memberId + ":tradeCode";

        // 防止并发情况下的一key多用，使用lua脚本在查询到同时删除
        String tradeCodeFromCache = jedis.get(tradeKey);

        if (tradeCode.equals(tradeCodeFromCache) && StringUtils.isNotBlank(tradeCodeFromCache)) {

            // 交易码是一次性的，查询正确后删除
            jedis.del(tradeKey);

            return true;
        }

        jedis.close();

        return false;
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {

        // 保存订单
        omsOrderMapper.insertSelective(omsOrder);
        String id = omsOrder.getId();

        // 保存订单详情表
        List<OmsOrderItem> orderItems = omsOrder.getOrderItems();

        for (OmsOrderItem orderItem : orderItems) {

            orderItem.setOrderId(id);
            omsOrderItemMapper.insertSelective(orderItem);
            // 删除购物车数据
            cartService.delCartItem(orderItem);

        }

    }

    @Override
    public OmsOrder getOrderByoutTradeId(String outTradeNO) {

        OmsOrder omsOrder=new OmsOrder();
        omsOrder.setOrderSn(outTradeNO);

        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);

        return omsOrder1;
    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {

        OmsOrder omsOrder1=new OmsOrder();
        omsOrder1.setStatus(1);

        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn", omsOrder.getOrderSn());

        Connection connection = null;
        Session session = null;

        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);// 开启mq事物，支持回滚
        } catch (JMSException e) {
            e.printStackTrace();
        }

        try {

            omsOrderMapper.updateByExampleSelective(omsOrder1, example);
            // 支付成功后引起的系统服务->订单服务更新->库存服务->物流
            // 发送一个订单已支付的队列，提供给库存消费
            // 调用mq发送更新成功消息

            Queue payment_success_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);

            //TextMessage textMessage = new ActiveMQTextMessage();// 字符串文本
            MapMessage mapMessage = new ActiveMQMapMessage();// hash结构

            mapMessage.setString("out_trade_no", omsOrder1.getOrderSn());

            producer.send(mapMessage);

            session.commit();

        } catch (Exception e) {
            // 数据库异常，消息回滚，保持事物一致性
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }


}
