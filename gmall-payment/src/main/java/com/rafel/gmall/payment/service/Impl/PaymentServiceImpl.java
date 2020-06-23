package com.rafel.gmall.payment.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.rafel.gmall.bean.PaymentInfo;
import com.rafel.gmall.mq.ActiveMQUtil;
import com.rafel.gmall.payment.config.AlipayConfig;
import com.rafel.gmall.payment.mapper.PaymentInfoMapper;
import com.rafel.gmall.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);

    }


    @Override
    public void updatePaymentInfo(PaymentInfo paymentInfo) {

        // 幂等性检查
        PaymentInfo paymentInfoParam = new PaymentInfo();
        paymentInfoParam.setOutTradeNo(paymentInfo.getOutTradeNo());
        PaymentInfo paymentInfo1 = paymentInfoMapper.selectOne(paymentInfoParam);

        if (StringUtils.isNotBlank(paymentInfo1.getPaymentStatus()) && paymentInfo1.getPaymentStatus().equals("已支付")) {
            return;
        }

        String outTradeNo = paymentInfo.getOutTradeNo();

        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", outTradeNo);

        Connection connection = null;
        Session session = null;

        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);// 开启mq事物，支持回滚
        } catch (JMSException e) {
            e.printStackTrace();
        }

        try {

            paymentInfoMapper.updateByExampleSelective(paymentInfo, example);
            // 支付成功后引起的系统服务->订单服务更新->库存服务->物流
            // 调用mq发送支付成功消息

            Queue payment_success_queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);

            //TextMessage textMessage = new ActiveMQTextMessage();// 字符串文本
            MapMessage mapMessage = new ActiveMQMapMessage();// hash结构

            mapMessage.setString("out_trade_no", paymentInfo.getOutTradeNo());

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

    @Override
    public void sendDelayPaymentResultCheckQueue(String outTradeNO) {

        Connection connection = null;
        Session session = null;

        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);// 开启mq事物，支持回滚
        } catch (JMSException e) {
            e.printStackTrace();
        }

        try {

//            omsOrderMapper.updateByExampleSelective(omsOrder1, example);
            // 支付成功后引起的系统服务->订单服务更新->库存服务->物流

            Queue payment_success_queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);

            //TextMessage textMessage = new ActiveMQTextMessage();// 字符串文本
            MapMessage mapMessage = new ActiveMQMapMessage();// hash结构

            mapMessage.setString("out_trade_no", outTradeNO);

            // 设置延迟队列
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 1000 * 30);

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

    @Override
    public Map<String, Object> checkAlipayPayment(String out_trade_no) {

        // 创建api对应的request
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();

        HashMap<String, Object> map = new HashMap<>();

        HashMap<String, Object> responseMap = new HashMap<>();

        map.put("out_trade_no", out_trade_no);

        alipayRequest.setBizContent(JSON.toJSONString(map));

        AlipayTradeQueryResponse response = null;

        try {
            response = alipayClient.execute(alipayRequest); //调用SDK
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if (response.isSuccess()) {
            System.out.println("交易已经创建，调用成功");

            responseMap.put("out_trade_no", response.getOutTradeNo());
            responseMap.put("trade_no", response.getTradeNo());
            responseMap.put("trade_status", response.getTradeStatus());
            responseMap.put("call_back_content", response.getMsg());

        } else {
            System.out.println("交易未创建，调用失败,resultMap为空");
        }

        return responseMap;
    }
}
