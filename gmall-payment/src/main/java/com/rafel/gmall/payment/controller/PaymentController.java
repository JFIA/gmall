package com.rafel.gmall.payment.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.rafel.gmall.annotations.LoginRequired;
import com.rafel.gmall.bean.OmsOrder;
import com.rafel.gmall.bean.PaymentInfo;
import com.rafel.gmall.payment.config.AlipayConfig;
import com.rafel.gmall.service.OrderService;
import com.rafel.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;


@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentService paymentService;

    @Reference
    OrderService orderService;


    // 支付成功后异步回调
    @RequestMapping("alipay/callback/return")
    @LoginRequired
    public String aliPayCallbackReturn(HttpServletRequest request, ModelMap modelMap) {

        // 回调请求获取支付宝参数
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String sign = request.getParameter("sign");
        String call_back_content = request.getQueryString();



        // 通过支付宝的paramsMap进行签名验证，2.0版本的接口将paramsMap参数去掉了，导致同步请求没法验签
        if (StringUtils.isNotBlank(sign)) {
            // 验签成功，更新用户状态到数据库中
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setCallbackContent(call_back_content);
            paymentInfo.setCallbackTime(new Date());

            paymentService.updatePaymentInfo(paymentInfo);

        }

        return "finish";
    }

    @RequestMapping("alipay/submit")
    @ResponseBody
    public String aliSubmit(String outTradeNO, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap) {

        String form = null;

        // 创建api对应的request
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();

        HashMap<String, Object> map = new HashMap<>();

        map.put("out_trade_no", outTradeNO);
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", totalAmount);
        map.put("subject", "mate20pro");

        alipayRequest.setBizContent(JSON.toJSONString(map));

        // 回调函数
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        // 生成并且保存用户的支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        OmsOrder omsOrder = orderService.getOrderByoutTradeId(outTradeNO);
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOutTradeNo(outTradeNO);
        paymentInfo.setSubject("mate20pro");
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setTotalAmount(totalAmount);

        paymentService.savePaymentInfo(paymentInfo);

        // 向消息中间件发送一个检查支付状态的延迟消息队列
        paymentService.sendDelayPaymentResultCheckQueue(outTradeNO);

        // 提交请求到支付宝

        return form;

    }

    @RequestMapping("mx/submit")
    @ResponseBody
    public String mxSubmit() {

        return null;

    }

    @RequestMapping("index")
    @LoginRequired
    public String index(String outTradeNO, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        modelMap.put("nickname", nickname);
        modelMap.put("outTradeNo", outTradeNO);
        modelMap.put("totalAmount", totalAmount);

        return "index";
    }

}
