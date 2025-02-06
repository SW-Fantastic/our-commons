package org.swdc.ours.common.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface Header {

    /**
     * HttpHeader的key，必须指定。
     */
    String key();

    /**
     * HttpHeader的值，默认为空字符串。如果为空则会从参数中取值。
     */
    String value() default "";


}
