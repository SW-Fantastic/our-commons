package org.swdc.ours.common.type;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.InputStream;

public class JSONMapper {

    private static ObjectMapper mapper;

    public static String writeString(Object obj) {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] writeBytes(Object obj) {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        }
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T readAsType(InputStream is, Class<T> returnType) {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        }
        try {
            return mapper.readValue(is, returnType);
        } catch (Exception e) {
            return null;
        }
    }
}
