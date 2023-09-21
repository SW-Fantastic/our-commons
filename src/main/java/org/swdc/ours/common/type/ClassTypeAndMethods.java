package org.swdc.ours.common.type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.util.*;


/**
 * 与Class/Type等Java类型相关的通用反射方法。
 */
public class ClassTypeAndMethods {

    /**
     * Lambda表达式转换为SerializedLambda对象。
     * @param lambda lambda
     * @return 转换结果
     */
    public static SerializedLambda extractSerializedLambda(Object lambda) {
        try {
            Method method = lambda.getClass()
                    .getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            return (SerializedLambda) method.invoke(lambda);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据Field的名字推测Getter并尝试获取对应的Method。
     * @param field 字段
     * @return 字段的Getter
     */
    public static Method extractGetter(Field field) {
        Class declareOn = field.getDeclaringClass();
        Class fieldType = field.getType();

        String name = field.getName();
        name = name.substring(0,1).toUpperCase() + name.substring(1);
        if (fieldType == boolean.class) {
            name = "is" + name;
        } else {
            name = "get" + name;
        }
        try {
            return declareOn.getMethod(name);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据字段的名字和类型推测Setter并且尝试获取此Method。
     * @param field 字段
     * @return 字段的Setter。
     */
    public static Method extractSetter(Field field) {
        Class declareOn = field.getDeclaringClass();
        Class fieldType = field.getType();

        String name = field.getName();
        name = "set" + name.substring(0,1).toUpperCase() + name.substring(1);
        try {
            return declareOn.getMethod(name,fieldType);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 读取一个Class的所有Method。
     * @param clazz 类型（Class对象）
     * @return 方法列表
     */
    public static List<Method> findAllMethods(Class clazz) {
        List<Method> methodList = new ArrayList<>();
        Class current = clazz;

        while (current != null) {
            if (current == Object.class) {
                break;
            }
            Method[] methods = current.getMethods();
            for (Method method: methods) {
                methodList.add(method);
            }
            current = current.getSuperclass();
        }
        return methodList;
    }

    /**
     * 判断类型是否为基本类型
     * @param type class对象
     * @return 是否为基本类型
     */
    public static boolean isBasicType(Class type) {
        if (type == int.class ||
                type == float.class ||
                type == double.class ||
                type == char.class ||
                type == byte.class ||
                type == short.class) {
            return  true;
        }
        return  false;
    }

    /**
     * 判断类型是否为被包装的基本类型。
     * @param type class对象
     * @return 是否为包装类型
     */
    public static boolean isBoxedType(Class type) {
        if (type == Integer.class ||
                type == Float.class ||
                type == Double.class ||
                type == Character.class ||
                type == Byte.class ||
                type == Boolean.class||
                type == Short.class) {
            return  true;
        }
        return  false;
    }

    /**
     * 通过包装类型获取对应的基本类型
     * @param type 包装类型的Class对象。
     * @return 基本类型的Class对象。
     */
    public static Class getBasicType(Class type){
        if (isBoxedType(type)) {
            if (Integer.class.equals(type)) {
                return int.class;
            } else if (Double.class.equals(type)) {
                return double.class;
            } else if (Float.class.equals(type)) {
                return float.class;
            } else if (Character.class.equals(type)) {
                return char.class;
            } else if (Byte.class.equals(type)) {
                return byte.class;
            } else if (Boolean.class.equals(type)){
                return boolean.class;
            } else if (Short.class.equals(type)) {
                return short.class;
            }
        } else if (isBasicType(type)) {
            return type;
        }
        throw new RuntimeException(type.getName() + "不是一个包装类型，无法进行转换");
    }


    /**
     * 判断此类是否为集合类型
     * @param clazz class对象
     * @return 是否为集合类型
     */
    public static Boolean isCollectionType(Class clazz) {
        return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz);
    }

    /**
     * 判断此类是否为List类型
     * @param clazz 类对象
     * @return 是否为List类型
     */
    public static Boolean isList(Class clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    /**
     * 判断此类是否为Map类型
     * @param clazz 类对象
     * @return 是否为Map类型
     */
    public static Boolean isMap(Class clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    /**
     * 读取此Field类型的泛型的真实类型。
     * @param field Field对象
     * @return 真实类型的Class对象列表
     */
    public static List<Class> getFieldParameters(Field field) {
        ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
        Type[] types = parameterizedType.getActualTypeArguments();
        List<Class> classes = new ArrayList<>();
        if (types == null || types.length == 0) {
            return classes;
        }
        for (Type type : types) {
            classes.add((Class) type);
        }
        return classes;
    }


    /**
     * 通过Jackson的ObjectMapper解码String，让他可以作为Method
     * 的参数被使用。
     *
     * @param method 方法对象
     * @param param String类型的参数列表
     * @return 可供Method使用的参数数组
     */
    public static Object[] convertStringAsParameters(Method method, List<String> param) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        Object[] result = new Object[param.size()];
        Parameter[] parameters = method.getParameters();
        if (param.size() != parameters.length) {
            return null;
        }
        for (int idx = 0; idx < parameters.length; idx ++) {
            Parameter theParam = parameters[idx];
            Class paramType = theParam.getType();
            try {
                if (theParam.getParameterizedType() instanceof ParameterizedType) {
                    ParameterizedType type = (ParameterizedType) theParam.getParameterizedType();
                    Type[] types = type.getActualTypeArguments();
                    Class[] paramTypes = new Class[types.length];
                    for (int typeParamIdx = 0; typeParamIdx < types.length; typeParamIdx ++) {
                        paramTypes[typeParamIdx] = (Class) types[typeParamIdx];
                    }
                    JavaType jtype = mapper.getTypeFactory().constructParametricType((Class<?>) type.getRawType(),paramTypes);
                    result[idx] = mapper.readValue(param.get(idx), jtype);
                } else {
                    result[idx] = mapper.readValue(param.get(idx), paramType);
                }
            } catch (Exception e) {
                result[idx] = null;
            }
        }
        return result;
    }


}
