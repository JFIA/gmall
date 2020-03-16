package com.rafel.gmall.intercetpors;

import com.alibaba.fastjson.JSON;
import com.rafel.gmall.annotations.LoginRequired;
import com.rafel.gmall.util.CookieUtil;
import com.rafel.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    // handler表示请求中代表的方法
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 拦截代码

        // 判断被拦截请求的访问的方法的注解（是否需要拦截的）
        // 反射得到方法的注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        LoginRequired methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequired.class);

        // 是否拦截
        if (methodAnnotation == null) {
            return true;
        }

        String token = "";

        // oldtoken代表之前登陆过
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }

        // newtoken代表地址栏携带验证过后的token
        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;

        }


        boolean loginSuccess = methodAnnotation.loginSuccess();// 获取该请求是否必须登陆成功,来决定拦截器拦截成功请求是否继续访问

        String success = "fail";
        Map<String, String> map = new HashMap<>();

        if (StringUtils.isNotBlank(token)) {

            // 调用认证中心验证

            String ip = "127.0.0.1";
            if (StringUtils.isNotBlank(request.getRemoteHost())) {
                ip = request.getRemoteHost();
            }
            String successJson = HttpclientUtil.doGet("http://127.0.0.1:8085/verify?token=" + token + "&currentIp=" + ip);
            map = JSON.parseObject(successJson, Map.class);
            success=map.get("status");

        }

        if (loginSuccess) {
            //必须登陆成功

            if (!success.equals("success")) {
                // 重定向passport登陆
                response.sendRedirect("http://127.0.0.1:8085/index?ReturnUrl=" + request.getRequestURL());
                return false;


            } else {

                // 将token携带的信息写入
                request.setAttribute("memberId", map.get("memberId"));
                request.setAttribute("nickname", map.get("nickname"));

                if (StringUtils.isNotBlank(token)) {
                    // 覆盖cookie中token
                    CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
                }
            }

        } else {
            // 没有登陆也能用，但是购物车结账时必须验证
            if (success.equals("success")) {
                // 将token携带的信息写入
                request.setAttribute("memberId", map.get("memberId"));
                request.setAttribute("nickname", map.get("nickname"));

                if (StringUtils.isNotBlank(token)) {
                    // 覆盖cookie中token
                    CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
                }
            }

        }


        return true;
    }
}