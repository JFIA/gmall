package com.rafel.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.rafel.gmall.annotations.LoginRequired;
import com.rafel.gmall.bean.OmsCartItem;
import com.rafel.gmall.bean.OmsOrder;
import com.rafel.gmall.bean.OmsOrderItem;
import com.rafel.gmall.bean.UmsMemberReceiveAddress;
import com.rafel.gmall.service.CartService;
import com.rafel.gmall.service.OrderService;
import com.rafel.gmall.service.SkuService;
import com.rafel.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Controller
public class OrderController {


    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;

    @RequestMapping("submitOrder")
    @LoginRequired
    public ModelAndView submitOrder(String receiveAddressId, BigDecimal totalAmount, HttpServletRequest request, HttpServletResponse response, String tradeCode) {

        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickename");

        boolean success = orderService.checkTradeCode(memberId, tradeCode);

        // 检验交易码,为了保证订单不重复提交
        if (success) {

            // 外部订单号,用来和其他系统交互，防止重复
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMDDHHmmss");
            String outTradeNo = "gmall" + System.currentTimeMillis() + simpleDateFormat.format(new Date());

            ArrayList<OmsOrderItem> orderItems = new ArrayList<>();

            // 订单对象
            OmsOrder omsOrder = new OmsOrder();

            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setMemberUsername(nickName);
            omsOrder.setMemberId(memberId);
            omsOrder.setOrderSn(outTradeNo);
            omsOrder.setPayAmount(totalAmount);
            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getReceiveAddressById(receiveAddressId);
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setStatus(0);
            omsOrder.setSourceType(0);


            // 根据用户ID获得要购买的商品列表和总价格
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);

            for (OmsCartItem omsCartItem : omsCartItems) {
                // 购物车商品被选中才购买,封装成单个订单物品对象
                if (omsCartItem.IsChecked().equals("1")) {

                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    // 生成订单时验价
                    boolean status = skuService.checkPrice(omsCartItem.getProductSkuId(), omsCartItem.getPrice());
                    if (!status) {

                        return new ModelAndView("tradeFail");
                    }
                    // 验库存,远程调用库存系统
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());

                    omsOrderItem.setOrderSn(outTradeNo);
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSn("仓库对应商品编号");

                    orderItems.add(omsOrderItem);
                }

            }

            omsOrder.setOrderItems(orderItems);

            // 将订单详情写入数据库
            // 删除购物车对应商品
            orderService.saveOrder(omsOrder);

            // 重定向到支付系统
            ModelAndView mv=new ModelAndView("redirect:http://127.0.0.1:8087/index");
            // 真实情况不需要传递参数，直接通过memberid查询，因为get请求容易被修改
            mv.addObject("outTradeNO",outTradeNo);
            mv.addObject("totalAmount",totalAmount);

            return mv;

        } else {

            return new ModelAndView("tradeFail");

        }

    }

    @RequestMapping("toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickename");

        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);

        List<UmsMemberReceiveAddress> receiveAddress = userService.getReceiveAddress(memberId);

        List<OmsOrderItem> omsOrderItems = new ArrayList<>();

        for (OmsCartItem omsCartItem : omsCartItems) {
            // 循环每一个购物车对象，将其商品详情封装成OmsOrderItem
            OmsOrderItem omsOrderItem = new OmsOrderItem();
            omsOrderItem.setProductName(omsCartItem.getProductName());
            omsCartItem.setProductPic(omsCartItem.getProductPic());

            omsOrderItems.add(omsOrderItem);
        }

        modelMap.put("orderDetailList", omsOrderItems);

        // 生成交易码，为了在提交订单时对交易码校验
        String tradeCode = orderService.genTradeCode(memberId);

        modelMap.put("tradeCode", tradeCode);

        return "trade";
    }

}
