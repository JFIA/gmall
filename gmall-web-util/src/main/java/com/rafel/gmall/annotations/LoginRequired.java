package com.rafel.gmall.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {

    // true代表拦截校验必须通过，false表示拦截器校验不通过也可以继续访问
    boolean loginSuccess() default true;

}
