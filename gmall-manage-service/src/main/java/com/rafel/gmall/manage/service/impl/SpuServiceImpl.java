package com.rafel.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.rafel.gmall.bean.*;
import com.rafel.gmall.manage.mapper.PmsProductImageMapper;
import com.rafel.gmall.manage.mapper.SpuProductInfoMapper;
import com.rafel.gmall.manage.mapper.SpuSaleAttrMapper;
import com.rafel.gmall.manage.mapper.SpuSaleAttrValueMapper;
import com.rafel.gmall.service.SpuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;


@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    SpuProductInfoMapper spuProductInfoMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    PmsProductImageMapper pmsProductImageMapper;



    @Override
    public List<PmsProductInfo> getSpuInfo(String catalog3Id) {

        Example e=new Example(PmsProductInfo.class);
        e.createCriteria().andEqualTo("catalog3Id", catalog3Id);

        return spuProductInfoMapper.selectByExample(e);
    }

    @Override
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {

        if (StringUtils.isBlank(pmsProductInfo.getId())){

            //id为空，保存属性。
            spuProductInfoMapper.insertSelective(pmsProductInfo);//insert，insertSelective是否将null插入，后者不为null插入。

            //保存属性值。
            List<PmsProductSaleAttr> attrSaleList=pmsProductInfo.getSpuSaleAttrList();

            //设置外健。
            for (PmsProductSaleAttr pmsProductSaleAttr:attrSaleList){
                pmsProductSaleAttr.setProductId(pmsProductInfo.getId());

                spuSaleAttrMapper.insertSelective(pmsProductSaleAttr);

                for (PmsProductSaleAttrValue pmsProductSaleAttrValue:pmsProductSaleAttr.getSpuSaleAttrValueList()){

                    pmsProductSaleAttrValue.setProductId(pmsProductInfo.getId());

                    spuSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);

                }
            }

        }

        else {
            //id不空，修改属性。
            Example e=new Example(PmsProductSaleAttr.class);

            e.createCriteria().andEqualTo("id", pmsProductInfo.getId());

            spuProductInfoMapper.updateByExampleSelective(pmsProductInfo,e);

            //修改属性值。
            List<PmsProductSaleAttr> attrSaleList=pmsProductInfo.getSpuSaleAttrList();

            Example example=new Example(PmsBaseAttrValue.class);
            example.createCriteria().andEqualTo("id", pmsProductInfo.getId());

            spuSaleAttrMapper.deleteByExample(example);

            for (PmsProductSaleAttr pmsProductSaleAttr:attrSaleList) {

                spuSaleAttrMapper.insertSelective(pmsProductSaleAttr);

            }

        }


        return "success";
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {

        Example e=new Example(PmsProductSaleAttr.class);
        e.createCriteria().andEqualTo("productId",spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrs=spuSaleAttrMapper.selectByExample(e);

        for (PmsProductSaleAttr pmsProductSaleAttr:pmsProductSaleAttrs){

            PmsProductSaleAttrValue pmsProductSaleAttrValue=new PmsProductSaleAttrValue();

            pmsProductSaleAttrValue.setSaleAttrId(pmsProductSaleAttr.getSaleAttrId());
            pmsProductSaleAttrValue.setProductId(spuId);

            pmsProductSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValueMapper.select(pmsProductSaleAttrValue));
        }

        return pmsProductSaleAttrs;
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {

        Example e=new Example(PmsProductImage.class);

        e.createCriteria().andEqualTo("productId",spuId);

        List<PmsProductImage> pmsProductImages=pmsProductImageMapper.selectByExample(e);

        return pmsProductImages;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId, String skuId) {

//        Example e=new Example(PmsProductSaleAttr.class);
//        e.createCriteria().andEqualTo("productId", productId);
//        List<PmsProductSaleAttr> pmsProductSaleAttrs =spuSaleAttrMapper.selectByExample(e);
//
//        for(PmsProductSaleAttr pmsProductSaleAttr:pmsProductSaleAttrs){
//
//            PmsProductSaleAttrValue pmsProductSaleAttrValue=new PmsProductSaleAttrValue();
//
//            pmsProductSaleAttrValue.setSaleAttrId(pmsProductSaleAttr.getSaleAttrId());
//            pmsProductSaleAttrValue.setProductId(productId);
//
//            pmsProductSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValueMapper.select(pmsProductSaleAttrValue));
//        }

        List<PmsProductSaleAttr> pmsProductSaleAttrs=spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(productId,skuId);

        return pmsProductSaleAttrs;
    }
}
