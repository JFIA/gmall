package com.rafel.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.rafel.gmall.bean.PmsProductImage;
import com.rafel.gmall.bean.PmsProductInfo;
import com.rafel.gmall.bean.PmsProductSaleAttr;
import com.rafel.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuService;

    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(@RequestParam String catalog3Id){
        List<PmsProductInfo> pmsProductInfos = spuService.getSpuInfo(catalog3Id);

        return pmsProductInfos;
    }

    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam MultipartFile multipartFile){

        String url=multipartFile.getOriginalFilename();
        return url;

    }

    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
        String status=spuService.saveSpuInfo(pmsProductInfo);

        return status;
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(@RequestParam String spuId){

        List<PmsProductSaleAttr> pmsProductSaleAttrs=spuService.spuSaleAttrList(spuId);

        return pmsProductSaleAttrs;
    }

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(@RequestParam String spuId){

        List<PmsProductImage> pmsProductImages=spuService.spuImageList(spuId);

        return pmsProductImages;

    }

}
