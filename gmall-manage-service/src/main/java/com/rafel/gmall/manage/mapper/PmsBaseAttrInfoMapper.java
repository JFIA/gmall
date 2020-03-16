package com.rafel.gmall.manage.mapper;

import com.rafel.gmall.bean.PmsBaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsBaseAttrInfoMapper extends Mapper<PmsBaseAttrInfo> {
    List<PmsBaseAttrInfo> selectSaleAttrListByValueId(@Param("valueIdStr") String valueIdStr);
}
