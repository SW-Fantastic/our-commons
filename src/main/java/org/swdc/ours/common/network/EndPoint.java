package org.swdc.ours.common.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个方法为网络请求的端点。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EndPoint {

    /**
     * URL，HTTP的请求会发送到这里。
     */
    String url();

    /**
     * 请求方法
     */
    Methods method();

}
