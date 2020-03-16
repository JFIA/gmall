package com.rafel.gmall.payment.mq;

import com.rafel.gmall.bean.OmsOrder;
import com.rafel.gmall.bean.PaymentInfo;
import com.rafel.gmall.service.PaymentService;
import javafx.beans.binding.ObjectExpression;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

public class PaymentServiceMQLIstener {

    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {
        String out_trade_no = mapMessage.getString("out_trade_no");

        // 调用paymentService的支付宝检查借口，判断map是否为空
        Map<String, Object> resultMap = paymentService.checkAlipayPayment(out_trade_no);

        if (resultMap == null || resultMap.isEmpty()) {
            // 未检查成功，重新发送消息队列
            // 需发送延迟队列
            paymentService.sendDelayPaymentResultCheckQueue(out_trade_no);

        } else {

            String trade_status = (String) resultMap.get("trade_status");

            // 根据结果判断接下去的下一次延迟队列还是更新数据

            if (StringUtils.isNotBlank(trade_status) && trade_status.equals("TRADE_SUCCESS")) {

                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOutTradeNo(out_trade_no);
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setAlipayTradeNo((String) resultMap.get("out_trade_no"));
                paymentInfo.setCallbackContent((String) resultMap.get("call_back_content"));
                paymentInfo.setCallbackTime(new Date());

                paymentService.updatePaymentInfo(paymentInfo);

            } else {
                // 需发送延迟队列
                paymentService.sendDelayPaymentResultCheckQueue(out_trade_no);
            }

        }

    }
}
