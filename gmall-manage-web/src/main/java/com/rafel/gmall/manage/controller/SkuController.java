package com.rafel.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.rafel.gmall.bean.PmsSkuInfo;
import com.rafel.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class SkuController {

    @Reference
    SkuService skuService;

    @RequestMapping("saveSkuInfo")
    @ResponseBody
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){

        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());

        String status = skuService.saveSkuInfo(pmsSkuInfo);
        String skuDefaultImg=pmsSkuInfo.getSkuDefaultImg();

        //前台未选默认图片，默认第一张为默认图片。
        if (StringUtils.isBlank(skuDefaultImg)){
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
        }

        return status;
    }


}
