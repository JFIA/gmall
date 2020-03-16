package com.rafel.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.rafel.gmall.bean.PmsBaseAttrInfo;
import com.rafel.gmall.bean.PmsBaseAttrValue;
import com.rafel.gmall.bean.PmsBaseSaleAttr;
import com.rafel.gmall.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@Controller
public class AttrController {

    @Reference
    AttrService attrService;

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<PmsBaseAttrInfo> getAttrInfo(@RequestParam String catalog3Id){

        List<PmsBaseAttrInfo> pmsBaseAttrInfos=attrService.getAttrInfo(catalog3Id);

        return pmsBaseAttrInfos;
    }

    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){

        String status=attrService.saveAttrInfo(pmsBaseAttrInfo);

        return status;

    }

    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue> getAttrValueList(@RequestParam String attrId){

        List<PmsBaseAttrValue> pmsBaseAttrValues=attrService.getAttrValue(attrId);

        return pmsBaseAttrValues;
    }

    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> getSaleAttrList(){

        List<PmsBaseSaleAttr> pmsBaseSaleAttrs=attrService.getSaleAttrList();

        return pmsBaseSaleAttrs;
    }

}
