package com.rafel.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.rafel.gmall.bean.PmsBaseCatalog1;
import com.rafel.gmall.bean.PmsBaseCatalog2;
import com.rafel.gmall.bean.PmsBaseCatalog3;
import com.rafel.gmall.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//解决跨域问题。
@CrossOrigin
@Controller
public class CatlogController{
    
    //相当于autowired，前者是在同一个web容器中注入，后者是通过协议远程注入。
    @Reference
    CatalogService catalogService;

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1(){

        List<PmsBaseCatalog1> catalog1s = catalogService.getCatalog1();

        return catalog1s;
    }

    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatalog2(@RequestParam String catalog1Id){

        List<PmsBaseCatalog2> catalog2s = catalogService.getCatalog2(catalog1Id);
        return catalog2s;
    }

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatalog3(@RequestParam String catalog2Id){

        List<PmsBaseCatalog3> catalog3s = catalogService.getCatalog3(catalog2Id);
        return catalog3s;
    }



}
