package com.rafel.gmall.service;

import com.rafel.gmall.bean.OmsCartItem;
import com.rafel.gmall.bean.OmsOrderItem;

import java.util.List;

public interface CartService {
    List<OmsCartItem> cartList(String memberId);

    void delCartItem(OmsOrderItem orderItem);
}
