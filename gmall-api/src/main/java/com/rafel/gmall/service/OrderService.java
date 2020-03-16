package com.rafel.gmall.service;

import com.rafel.gmall.bean.OmsOrder;

public interface OrderService {
    String genTradeCode(String memberId);

    boolean checkTradeCode(String memberId,String tradeCode);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByoutTradeId(String outTradeNO);

    void updateOrder(OmsOrder omsOrder);
}
