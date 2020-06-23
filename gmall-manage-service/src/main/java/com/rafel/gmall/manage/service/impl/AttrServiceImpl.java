package com.rafel.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.rafel.gmall.bean.PmsBaseAttrInfo;
import com.rafel.gmall.bean.PmsBaseAttrValue;
import com.rafel.gmall.bean.PmsBaseSaleAttr;
import com.rafel.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.rafel.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.rafel.gmall.manage.mapper.PmsBaseSaleAttrMapper;
import com.rafel.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.HashSet;
import java.util.List;


@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;

    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;


    @Override
    public List<PmsBaseAttrInfo> getAttrInfo(String catalog3Id) {

        Example e=new Example(PmsBaseAttrInfo.class);

        e.createCriteria().andEqualTo("catalog3Id", catalog3Id);

        List<PmsBaseAttrInfo> pmsBaseAttrInfos=pmsBaseAttrInfoMapper.selectByExample(e);

        for(PmsBaseAttrInfo pmsBaseAttrInfo:pmsBaseAttrInfos){

            Example example=new Example(PmsBaseAttrValue.class);

            example.createCriteria().andEqualTo("attrId", pmsBaseAttrInfo.getId());

            pmsBaseAttrInfo.setAttrValueList(pmsBaseAttrValueMapper.selectByExample(example));

        }

        return pmsBaseAttrInfos;
    }

    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        if (StringUtils.isBlank(pmsBaseAttrInfo.getId())){

            //id为空，保存属性。
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);//insert，insertSelective是否将null插入，后者不为null插入。

            //保存属性值。
            List<PmsBaseAttrValue> attrValueList=pmsBaseAttrInfo.getAttrValueList();

            //设置外健。
            for (PmsBaseAttrValue pmsBaseAttrValue:attrValueList){
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());

                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }

        }

        else {
            //id不空，修改属性。
            Example e=new Example(PmsBaseAttrInfo.class);

            e.createCriteria().andEqualTo("id", pmsBaseAttrInfo.getId());

            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo,e);

            //修改属性值。
            List<PmsBaseAttrValue> pmsBaseAttrValues=pmsBaseAttrInfo.getAttrValueList();

            Example example=new Example(PmsBaseAttrValue.class);
            example.createCriteria().andEqualTo("id", pmsBaseAttrInfo.getId());

            pmsBaseAttrValueMapper.deleteByExample(example);

            for (PmsBaseAttrValue pmsBaseAttrValue:pmsBaseAttrValues) {

                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);

            }

        }


        return "success";
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValue(String attrId) {

        Example e=new Example(PmsBaseAttrValue.class);
        e.createCriteria().andEqualTo("attrId", attrId);

        return pmsBaseAttrValueMapper.selectByExample(e);
    }

    @Override
    public List<PmsBaseSaleAttr> getSaleAttrList() {

        return pmsBaseSaleAttrMapper.selectAll();
    }

    @Override
    public List<PmsBaseAttrInfo> getSaleAttrListByValueId(HashSet<String> valueIdSet) {

        String valueIdStr = StringUtils.join(valueIdSet, ",");

        List<PmsBaseAttrInfo> pmsBaseAttrInfos=pmsBaseAttrInfoMapper.selectSaleAttrListByValueId(valueIdStr);

        return pmsBaseAttrInfos;
    }


}
