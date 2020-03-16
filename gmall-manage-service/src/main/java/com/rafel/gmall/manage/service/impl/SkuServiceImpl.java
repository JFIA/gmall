package com.rafel.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.rafel.gmall.bean.PmsSkuAttrValue;
import com.rafel.gmall.bean.PmsSkuImage;
import com.rafel.gmall.bean.PmsSkuInfo;
import com.rafel.gmall.bean.PmsSkuSaleAttrValue;
import com.rafel.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.rafel.gmall.manage.mapper.PmsSkuImageMapper;
import com.rafel.gmall.manage.mapper.PmsSkuInfoMapper;
import com.rafel.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.rafel.gmall.service.SkuService;
import com.rafel.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        // 插入skuInfo。
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();

        // 插入平台属性关联。
        List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : pmsSkuAttrValues) {

            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);

        }

        // 插入销售属性关联。
        List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValues = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuSaleAttrValues) {

            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

        // 插入图片信息。
        List<PmsSkuImage> pmsSkuImages = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : pmsSkuImages) {

            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

        return "success";
    }

    public PmsSkuInfo getBySkuIdFromDB(String skuId) {
        Example e = new Example(PmsSkuInfo.class);
        e.createCriteria().andEqualTo("id", skuId);
        PmsSkuInfo pmsSkuInfo = pmsSkuInfoMapper.selectOneByExample(e);

        Example example = new Example(PmsSkuImage.class);
        example.createCriteria().andEqualTo("skuId", skuId);
        pmsSkuInfo.setSkuImageList(pmsSkuImageMapper.selectByExample(example));

        return pmsSkuInfo;
    }

    @Override
    public PmsSkuInfo getByskuId(String skuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();

        // 连接缓存
        Jedis jedis = redisUtil.getJedis();
        // 查询缓存
        String skuKey = "sku:" + skuId + ":info";
        String skuJson = jedis.get(skuKey);

        if (StringUtils.isNotBlank(skuJson)) {
            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);

        } else {
            // 如果缓存中没有，查询mysql
            // 设置分布式锁
            String token = UUID.randomUUID().toString();
            String ok = jedis.set("sku:" + skuId + ":lock", token, "nx", "px", 60);
            if (StringUtils.isNotBlank(ok) && ok.equals("OK")) {
                pmsSkuInfo = getBySkuIdFromDB(skuId);
                if (pmsSkuInfo != null) {
                    // mysql查询结果存入redis
                    jedis.set(skuKey, JSON.toJSONString(pmsSkuInfo));
                } else {
                    // 数据库中不存在该sku
                    // 为了防止缓存穿透，将null或者空字符串设置给redis
                    jedis.setex(skuKey, 60 * 3, JSON.toJSONString(""));
                }

                // 访问完mysql后释放分布式锁
                String lockToken = jedis.get("sku:" + skuId + ":lock");
                if (StringUtils.isNotBlank(lockToken) && lockToken.equals(token)) {
                    // 用token确认删除的是自己的锁
                    jedis.del("sku:" + skuId + ":lock");
                }


            } else {
                // 设置失败,自旋即该线程睡眠几秒重新尝试访问本方法
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getByskuId(skuId);
            }

        }

        jedis.close();

        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);

        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSkuInfo() {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {

            String skuId = pmsSkuInfo.getId();

            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();

            pmsSkuAttrValue.setSkuId(skuId);

            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValueMapper.select(pmsSkuAttrValue));

        }
        return pmsSkuInfos;

    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal productPrice) {

        boolean b = false;

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        BigDecimal price = pmsSkuInfo1.getPrice();

        if (productPrice.compareTo(price) == 0) {
            b = true;
        }


        return b;
    }
}
