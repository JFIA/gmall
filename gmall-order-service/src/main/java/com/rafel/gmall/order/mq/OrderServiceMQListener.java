package com.rafel.gmall.order.mq;


import com.rafel.gmall.bean.OmsOrder;
import com.rafel.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderServiceMQListener {

    @Autowired
    OrderService orderService;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE",containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage){
        String out_trade_no = null;

        try {
            out_trade_no = mapMessage.getString("out_trade_no");
        } catch (JMSException e) {
            e.printStackTrace();
        }

        // 更新订单状态
        OmsOrder omsOrder=new OmsOrder();
        omsOrder.setOrderSn(out_trade_no);
        orderService.updateOrder(omsOrder);


    }

}
