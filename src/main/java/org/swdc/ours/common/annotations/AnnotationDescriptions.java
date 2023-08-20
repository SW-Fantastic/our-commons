package org.swdc.ours.common.annotations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AnnotationDescriptions {

    private Map<Class,AnnotationDescription> desc;

    AnnotationDescriptions(Map<Class,AnnotationDescription> desc) {
        this.desc = desc;
    }

    AnnotationDescriptions() {
        this.desc = new HashMap<>();
    }

    public Collection<AnnotationDescription> values() {
        return desc.values();
    }


    public AnnotationDescription get(Class annotationType) {
        if (annotationType.isAnnotation()) {
            return desc.get(annotationType);
        }
        return null;
    }

    public boolean containsKey(Class annotationType) {
        if (annotationType.isAnnotation()) {
            return desc.containsKey(annotationType);
        }
        return false;
    }

}
