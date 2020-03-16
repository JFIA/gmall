package com.rafel.gmall.service;

import com.rafel.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfo(PaymentInfo paymentInfo);

    void sendDelayPaymentResultCheckQueue(String outTradeNO);

    Map<String, Object> checkAlipayPayment(String out_trade_no);
}
