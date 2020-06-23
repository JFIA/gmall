package com.rafel.gmall.item.Controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.rafel.gmall.bean.PmsProductSaleAttr;
import com.rafel.gmall.bean.PmsSkuAttrValue;
import com.rafel.gmall.bean.PmsSkuInfo;
import com.rafel.gmall.bean.PmsSkuSaleAttrValue;
import com.rafel.gmall.service.SkuService;
import com.rafel.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

@Controller
@CrossOrigin
public class itemController {


    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable(value = "skuId") String skuId, ModelMap map) {

        PmsSkuInfo pmsSkuInfo=skuService.getByskuId(skuId);
        map.put("skuInfo", pmsSkuInfo);

        List<PmsProductSaleAttr> pmsProductSaleAttrs=spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(),skuId);
        map.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrs);

        HashMap<String,String> skuSaleAttrHash=new HashMap<>();
        //查询当前sku的spu其他sku集合的hash表
        //根据当前productId找到所有相关的PmsSkuInfo集合
        List<PmsSkuInfo>pmsSkuInfos=skuService.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String v = skuInfo.getId();
            String k="";

            List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValues = skuInfo.getSkuSaleAttrValueList();

            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue:pmsSkuSaleAttrValues) {

                k += pmsSkuSaleAttrValue.getSaleAttrId() + "|";

            }
            skuSaleAttrHash.put(k,v);

        }
        //将sku销售属性hash表放到页面
        String skuSaleAttrHash2Json = JSON.toJSONString(skuSaleAttrHash);
        map.put("skuSaleAttrHash2Json", skuSaleAttrHash2Json);

        return "item";
    }



}
