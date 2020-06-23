package com.rafel.gmall.user.Controller;


import com.rafel.gmall.service.UserService;
import com.rafel.gmall.bean.UmsMember;
import com.rafel.gmall.bean.UmsMemberReceiveAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
public class UserController {

    @Autowired
    UserService userService;


    @RequestMapping("getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser(){
        List<UmsMember> umsMember= userService.getAllUser();

        return umsMember;
    }


    @RequestMapping("getAddress")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getReceiveAdressById(@RequestParam String MemberId){

        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userService.getReceiveAddress(MemberId);

        return umsMemberReceiveAddresses;
    }

    @RequestMapping("index")
    @ResponseBody
    public String index(){
        return "hello user";
    }
}
