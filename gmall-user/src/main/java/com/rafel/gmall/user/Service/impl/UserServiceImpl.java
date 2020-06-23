package com.rafel.gmall.user.Service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.rafel.gmall.service.UserService;
import com.rafel.gmall.bean.UmsMember;
import com.rafel.gmall.bean.UmsMemberReceiveAddress;
import com.rafel.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.rafel.gmall.user.mapper.UserMapper;
import com.rafel.gmall.util.RedisUtil;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;


@Service
public class UserServiceImpl implements UserService {


    @Autowired
    UserMapper userMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMembers = userMapper.selectAll();//userMapper.selectAllUser();
        return umsMembers;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddress(String memberId) {

        //根据外健查询用example。
        Example e = new Example(UmsMemberReceiveAddress.class);

        //查询规则。
        e.createCriteria().andEqualTo("memberId", memberId);

        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.selectByExample(e);

        //UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        //umsMemberReceiveAddress.setMemberId(memberId);

        //List<UmsMemberReceiveAddress> umsMemberReceiveAddresses=umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);

        return umsMemberReceiveAddresses;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {

        Jedis jedis = null;

        try {

            jedis = redisUtil.getJedis();

            if (jedis != null) {
                // 缓存开启
                String umsMemberStr = jedis.get("user:" + umsMember.getPassword() + umsMember.getUsername() + ":password");

                if (StringUtils.isNotBlank(umsMemberStr)) {
                    // 密码正确
                    UmsMember umsMember1 = JSON.parseObject(umsMemberStr, UmsMember.class);

                    return umsMember1;

                } else {
                    // 密码错误或缓存中不存在，开启数据库
                    UmsMember umsMemberFromDb = loginFromDB(umsMember);

                    if (umsMemberFromDb != null) {
                        jedis.setex("user:" + umsMemberFromDb.getPassword() + ":password", 60 * 60 * 24, JSON.toJSONString(umsMemberFromDb));

                    }
                    return umsMemberFromDb;

                }
            } else {
                // redis宕机，redisson开启分布式锁访问数据库
                UmsMember umsMemberFromDb = loginFromDB(umsMember);

                if (umsMemberFromDb != null) {
                    jedis.setex("user:" + umsMemberFromDb.getPassword() + umsMemberFromDb.getUsername() + ":password", 60 * 60 * 24, JSON.toJSONString(umsMemberFromDb));

                }
                return umsMemberFromDb;

            }
        } finally {

            jedis.close();
        }

    }

    @Override
    public void addUserTokenToCache(String token, String memberId) {

        Jedis jedis = null;

        try {
            jedis = redisUtil.getJedis();
            jedis.setex("user:" + memberId + ":token", 60 * 60 * 2, token);

        } finally {
            jedis.close();
        }

    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId) {

        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();

        umsMemberReceiveAddress.setId(receiveAddressId);

        umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);

        return umsMemberReceiveAddress;
    }

    private UmsMember loginFromDB(UmsMember umsMember) {

        UmsMember umsMember1 = userMapper.selectOne(umsMember);

        return umsMember1;
    }
}
