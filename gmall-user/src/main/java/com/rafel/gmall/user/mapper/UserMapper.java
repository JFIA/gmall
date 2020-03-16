package com.rafel.gmall.user.mapper;

import com.rafel.gmall.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

//使用通用Mapper需要先继承。
public interface UserMapper extends Mapper<UmsMember> {

    List<UmsMember> selectAllUser();
}
