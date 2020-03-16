package com.rafel.gmall.search.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.rafel.gmall.annotations.LoginRequired;
import com.rafel.gmall.bean.*;
import com.rafel.gmall.service.AttrService;
import com.rafel.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


@Controller


public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    // 不写requestParam会自动将前端传的数据封装成对象
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) throws IOException {

        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.getList(pmsSearchParam);

        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);

        // 抽取搜索结果的平台属性集合
        HashSet<String> valueIdSet = new HashSet<>();

        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {

            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();

            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                valueIdSet.add(pmsSkuAttrValue.getValueId());

            }

        }
        // 根据id查询属性值

        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getSaleAttrListByValueId(valueIdSet);
        modelMap.put("attrList", pmsBaseAttrInfos);

        modelMap.put("urlParam", getURLParam(pmsSearchParam));

        // 面包屑
        ArrayList<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();

        String[] valueId = pmsSearchParam.getValueId();
        if (valueId != null) {
            // 不为空说明当前请求所有参数都会生成一个面包屑
            for (String s : valueId) {
                // 生成面包屑的参数
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                pmsSearchCrumb.setValueId(s);
                pmsSearchCrumb.setValueName(s);
                pmsSearchCrumb.setUrlParam(getURLParam(pmsSearchParam, s));

                pmsSearchCrumbs.add(pmsSearchCrumb);

            }

        }

        modelMap.put("attrValueSelectedList", pmsSearchCrumbs);


        return "list";
    }

    private String getURLParam(PmsSearchParam pmsSearchParam, String... valueId) {

        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] pmsSkuAttrValues = pmsSearchParam.getValueId();

        String urlParam = "";

        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        if (pmsSkuAttrValues != null) {

            for (String pmsSkuAttrValue : pmsSkuAttrValues) {

                // 面包屑url处理
                // 去掉面包屑属性值意味着属性值与剩下的都不相同，将除它以外剩下的再处理一遍URL即可
                if (!pmsSkuAttrValue.equals(valueId)) {

                    urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
                }

            }
        }

        return urlParam;
    }

    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String index() {

        return "index";
    }

}
