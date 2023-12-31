package org.swdc.ours.common.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Annotations {

    /**
     * 解析某个反射对象的注解
     * @param element 可被注解的对象
     * @return 注解描述列表，Key为注解类型，Value为注解描述符
     */
    public static AnnotationDescriptions getAnnotations(AnnotatedElement element) {
        Map<Class,AnnotationDescription> desc = new HashMap<>();
        Annotation[] annotations = element.getAnnotations();
        for (Annotation annotation: annotations) {
            Class clazz = annotation.annotationType();
            if (clazz == Target.class ||clazz == Retention.class || clazz == Documented.class) {
                continue;
            }

            AnnotationDescriptions parent = getAnnotations(clazz);

            AnnotationDescription annoDesc = new AnnotationDescription(clazz);
            List<Method> methods = getAnnotationMethods(clazz);

            for (AnnotationDescription description: parent.values()) {
                description.setDeclareOn(annoDesc);
            }

            for (Method method: methods) {
                try {
                    Object val = method.invoke(annotation);
                    Class propType = val.getClass();
                    String propName = method.getName();
                    annoDesc.putProperty(propType,propName,val);
                    AliasFor aliasFor = method.getAnnotation(AliasFor.class);
                    if (aliasFor != null) {
                        AnnotationDescription parentDesc = parent.get(aliasFor.annotation());
                        parentDesc.putProperty(propType,aliasFor.value(),val);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            annoDesc.setAnnotations(parent);
            desc.put(clazz,annoDesc);
        }
        return new AnnotationDescriptions(desc);
    }

    public static AnnotationDescription findAnnotationIn(AnnotationDescriptions map, Class annotation) {
        if (map.containsKey(annotation)) {
            return map.get(annotation);
        }
        for (AnnotationDescription desc: map.values()) {
            AnnotationDescription found = desc.find(annotation);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * 从一个可注解的反射对象中查找指定的注解。
     * @param elem 可标注注解的反射对象（Class，Field，Method等多种反射对象）
     * @param annotationType 注解类型
     * @return 注解描述
     */
    public static AnnotationDescription findAnnotation(AnnotatedElement elem, Class annotationType) {
        AnnotationDescriptions descriptionMap = getAnnotations(elem);
        return findAnnotationIn(descriptionMap,annotationType);
    }

    public static List<Field> findFieldsByAnnotation(Class clazz,Class annotatedWith) {
        List<Field> fieldList = new ArrayList<>();
        Class current = clazz;

        while (current != null) {
            Field[] fields = current.getDeclaredFields();
            for (Field field: fields) {
                AnnotationDescription desc = findAnnotation(field,annotatedWith);
                if (desc != null) {
                    fieldList.add(field);
                }
            }
            current = current.getSuperclass();
        }
        return fieldList;
    }

    /**
     * 读取标注了特定注解的字段。
     * @param type 从本类读取字段
     * @param annotationType 字段应当标注此注解
     * @return 字段列表
     */
    public static List<Field> getAnnotationField(Class type, Class annotationType) {
        List<Field> fields = new ArrayList<>();
        Class curr = type;
        while (curr != null) {
            Field[] currFields = curr.getDeclaredFields();
            for (Field field: currFields) {
                AnnotationDescriptions desc = getAnnotations(field);
                if (findAnnotationIn(desc,annotationType) != null) {
                    fields.add(field);
                }
            }
            curr = curr.getSuperclass();
        }
        return fields;
    }

    /**
     * 读取一个Annotation对象的Method。
     * 这里的method指的是注解对象的方法。
     * <div><code>
     * <pre>
     * public @interface Test{
     *  String value()
     * }
     * </pre>
     * </code></div>
     * 例如上述示例中的value()方法，那就是本方法读取的目标。
     *
     * @param annotationType
     * @return
     */
    private static List<Method> getAnnotationMethods(Class annotationType) {
        List<String> methodNames = Arrays.asList("toString","equals","annotationType","hashCode");

        return Stream.of(annotationType.getMethods())
                .filter(m -> m.getParameters().length == 0)
                .filter(m -> !methodNames.contains(m.getName()))
                .collect(Collectors.toList());
    }


}
