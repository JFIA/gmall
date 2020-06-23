package com.rafel.gmall.service;

import com.rafel.gmall.bean.PmsBaseAttrInfo;
import com.rafel.gmall.bean.PmsBaseAttrValue;
import com.rafel.gmall.bean.PmsBaseSaleAttr;

import java.util.HashSet;
import java.util.List;

public interface AttrService {

    List<PmsBaseAttrInfo> getAttrInfo(String catalog3Id);

    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> getAttrValue(String attrId);

    List<PmsBaseSaleAttr> getSaleAttrList();

    List<PmsBaseAttrInfo> getSaleAttrListByValueId(HashSet<String> valueIdSet);
}
