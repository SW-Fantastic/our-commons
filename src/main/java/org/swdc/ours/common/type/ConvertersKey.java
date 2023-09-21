package org.swdc.ours.common.type;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Converter的Key。
 * 是一种类Tuple类型，对于一个Converter来说，每两个顺序不同的Class为一组，
 * 本类应该通过静态方法返回对于每一组Class来说唯一的ConvertersKey对象。
 */
public class ConvertersKey {

    private static Map<Class,Map<Class,ConvertersKey>> keys = new ConcurrentHashMap();

    private Class classForm;
    private Class classTo;

    private ConvertersKey(Class form, Class to) {
        this.classForm = form;
        this.classTo = to;
    }

    public Class getClassForm() {
        return classForm;
    }

    public Class getClassTo() {
        return classTo;
    }

    public static ConvertersKey of(Class form, Class to) {
        if (keys.containsKey(form)) {
            Map<Class,ConvertersKey> map = keys.get(form);
            if (map.containsKey(to)) {
                return map.get(to);
            } else {
                ConvertersKey key = new ConvertersKey(form, to);
                map.put(to,key);
                return key;
            }
        } else {
            Map<Class,ConvertersKey> map = new HashMap<>();
            ConvertersKey key = new ConvertersKey(form, to);
            map.put(to,key);
            keys.put(form,map);
            return key;
        }
    }

}
