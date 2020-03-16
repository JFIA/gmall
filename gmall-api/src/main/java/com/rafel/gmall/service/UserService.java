package com.rafel.gmall.service;

import com.rafel.gmall.bean.UmsMember;
import com.rafel.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;


public interface UserService {

    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddress(String memberId);

    UmsMember login(UmsMember umsMember);

    void addUserTokenToCache(String token, String memberId);

    UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId);
}
