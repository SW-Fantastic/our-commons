package org.swdc.ours.common.type;

@FunctionalInterface
public interface Converter<T,R> {

    R convert(T t);

}
