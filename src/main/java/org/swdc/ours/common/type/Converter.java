package org.swdc.ours.common.type;

/**
 * 类型转换接口，通过convert方法达成泛型参数T到R的转换。
 *
 * @param <T> 一个类型
 * @param <R> 另一个类型
 */
@FunctionalInterface
public interface Converter<T,R> {

    /**
     * 类型转换的实现方法，参数是被转换T类型对象，返回需要R类型的结果对象。
     * @param t 被转换对象
     * @return 转换结果
     */
    R convert(T t);

}
