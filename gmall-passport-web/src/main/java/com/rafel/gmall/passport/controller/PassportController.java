package com.rafel.gmall.passport.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.rafel.gmall.bean.UmsMember;
import com.rafel.gmall.service.UserService;
import com.rafel.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {


    @Reference
    UserService userService;

    // 校验token真假
    @RequestMapping("verify")
    @ResponseBody
    // 调用currentIp是因为登陆时注册的ip和调用拦截器的应用ip不一定相同
    public String verify(@RequestParam String token, @RequestParam String currentIp) {

        // 通过jwt验证

        HashMap<String, String> map = new HashMap<>();

        Map<String, Object> gmall = JwtUtil.decode(token, "gmall", currentIp);

        if (gmall != null) {

            map.put("status", "success");

            map.put("memberId", (String) gmall.get("memberId"));
            map.put("nickname", (String) gmall.get("nickname"));
        } else {
            map.put("status", "fail");
        }

        return JSON.toJSONString(map);
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {

        String token;
        // 调用用户服务验证用户名和密码
        UmsMember umsMemberLogin = userService.login(umsMember);

        if (umsMemberLogin != null) {
            // 登陆成功，用jwt制作token
            String nickname = umsMember.getNickname();
            String memberId = umsMember.getId();

            HashMap<String, String> memberMap = new HashMap<>();
            memberMap.put("nickname", nickname);
            memberMap.put("memberId", memberId);

            String ip = "127.0.0.1";
            if (StringUtils.isNotBlank(request.getRemoteHost())) {
                ip = request.getRemoteHost();
            }

            token = JwtUtil.encode("gmall", memberMap, ip);

            // 存入redis
            userService.addUserTokenToCache(token, memberId);

        } else {
            token = "fail";

        }

        return token;
    }

    @RequestMapping("index")
    public String index(@RequestParam String ReturnUrl, ModelMap modelMap) {

        modelMap.put("ReturnUrl", ReturnUrl);

        return "index";
    }

}
