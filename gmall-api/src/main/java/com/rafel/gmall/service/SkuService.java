package com.rafel.gmall.service;

import com.rafel.gmall.bean.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {

    String saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getByskuId(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    List<PmsSkuInfo> getAllSkuInfo();

    boolean checkPrice(String productSkuId, BigDecimal productPrice);
}
