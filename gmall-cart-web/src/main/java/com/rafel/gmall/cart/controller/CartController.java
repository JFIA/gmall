package com.rafel.gmall.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.rafel.gmall.annotations.LoginRequired;
import com.rafel.gmall.bean.OmsCartItem;
import com.rafel.gmall.bean.PmsSkuInfo;
import com.rafel.gmall.service.CartService;
import com.rafel.gmall.service.SkuService;

import com.rafel.gmall.util.CookieUtil;
import groovy.util.logging.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;


    @RequestMapping("addToCart")
    @LoginRequired(loginSuccess = true)
    public String addToCart(@RequestParam String skuId, @RequestParam int quantity, HttpServletRequest request, HttpServletResponse response){

        // 调用商品服务查询商品查询
        PmsSkuInfo pmsSkuInfo = skuService.getByskuId(skuId);

        // 将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(pmsSkuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductId(pmsSkuInfo.getProductId());
        omsCartItem.setProductName(pmsSkuInfo.getSkuName());
        omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(quantity);


        // 判断用户是否登陆
        String memberId = (String) request.getAttribute("memberId");
        List<OmsCartItem> omsCartItems=new ArrayList<>();

        if (StringUtils.isBlank(memberId)){
            // 用户没有登陆操作cookie

            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isBlank(cartListCookie)){

                omsCartItems.add(omsCartItem);
            }else {
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                // 判断添加的购物车数据在cookie中是否存在
                boolean exist= cartExist(omsCartItems, omsCartItem);
                if (exist){
                    // 如果存在更新cookie购物车数据
                    for (OmsCartItem cartItem : omsCartItems) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                            cartItem.setQuantity(cartItem.getQuantity()+omsCartItem.getQuantity());
//                            cartItem.setPrice(cartItem.getPrice().add(omsCartItem.getPrice()));
                        }

                    }

                }else {

                    omsCartItems.add(omsCartItem);
                }

                // 覆盖客户端cookie
                CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(omsCartItems),60*60*72,true);

            }


        }else {
            // 用户已经登陆
            // 从db中选出购物车数据
            OmsCartItem omsCartItemFromDB =cartService.getCartExistByUser(memberId,skuId);

            if (omsCartItemFromDB==null) {
                // 数据库没有添加该商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setQuantity(quantity);
                cartService.addCart(omsCartItem);

            }else {
                omsCartItemFromDB.setQuantity(omsCartItem.getQuantity()+omsCartItemFromDB.getQuantity());
                // 更新数据库
                cartService.updateCart(omsCartItemFromDB);

            }

            // 同步缓存
            cartService.flushCartCache(memberId);

        }

        // post方法重定向，get方法直接return页面
        return "redirect:/success.html";
    }


    @RequestMapping("cartList")
    @LoginRequired(loginSuccess = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response){

        String userId="";

        if (StringUtils.isNotBlank(userId)){
            // 用户名不为空，从redis查询数据
            cartService.getCartList();
        }else {
            // 否则查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)){
                List<OmsCartItem>omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }


        }

        return "cartList";
    }

    private boolean cartExist(List<OmsCartItem> omsCartItems,OmsCartItem omsCartItem) {

        boolean b=false;
        for (OmsCartItem CartItem : omsCartItems) {
            if (omsCartItem.getProductSkuId().equals(CartItem.getProductSkuId())){
                b=true;
            }

        }
        return b;

    }

}
