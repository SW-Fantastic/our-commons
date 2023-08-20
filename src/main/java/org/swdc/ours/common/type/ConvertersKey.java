package org.swdc.ours.common.type;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
