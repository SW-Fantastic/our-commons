package org.swdc.ours.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamResources {

    /**
     * 将输入流转换为字符串
     *
     * @param in 输入流
     * @return 输入流对应的字符串，如果发生异常则返回null
     */
    public static String readStreamAsString(InputStream in) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 将输入流反序列化为指定类型的对象
     *
     * @param <T>       目标类型
     * @param target    目标类型的Class对象
     * @param in        输入流
     * @return          反序列化后的目标类型对象
     * @throws RuntimeException 如果在反序列化过程中发生异常，则抛出运行时异常
     */
    public static  <T> T  readStreamAs(Class<T> target, InputStream in) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            return mapper.readValue(in,target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
