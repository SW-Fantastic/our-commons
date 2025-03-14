package org.swdc.ours.common.network;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.swdc.ours.common.type.ClassTypeAndMethods;
import org.swdc.ours.common.type.Converter;
import org.swdc.ours.common.type.Converters;
import org.swdc.ours.common.type.TypedKey;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过动态代理的方式，创建网络请求的客户端
 */
public class Network implements InvocationHandler {

    private static ConcurrentHashMap<TypedKey, Object> instances = new ConcurrentHashMap<>();

    private static Converters converters = new Converters();

    private AuthProvider provider;

    private String baseUrl;

    private ObjectMapper mapper;

    private HttpRequester requester;

    private Network(AuthProvider provider, HttpRequester requester, String baseUrl) {

        this.requester = requester;
        this.provider = provider;
        this.baseUrl = baseUrl;
        this.mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Parameter[] parameters = method.getParameters();
        EndPoint endPoint = method.getAnnotation(EndPoint.class);
        if (endPoint == null) {
            throw new IllegalArgumentException("Method must be annotated with @EndPoint");
        }

        String url = "";
        if (endPoint.url().startsWith("/")) {
            url += endPoint.url();
        } else {
            url += "/" + endPoint.url();
        }

        Object requestBody = null;
        HttpHeaders headers = method.getAnnotation(HttpHeaders.class);
        Map<String, String> headersMap = new HashMap<>();
        if (headers != null) {
            for (Header header : headers.value()) {
                headersMap.put(header.key(), header.value());
            }
        }

        for (int paramIndex = 0; paramIndex < parameters.length; paramIndex++) {
            Parameter params = parameters[paramIndex];
            QueryString qs = params.getAnnotation(QueryString.class);
            HttpBody body = params.getAnnotation(HttpBody.class);
            Path path = params.getAnnotation(Path.class);
            Header header = params.getAnnotation(Header.class);

            if (qs != null && !qs.value().isEmpty()) {
                if (!url.contains("?")) {
                    url = url + "?";
                }
                url = url + qs.value() + "=" + args[paramIndex].toString();
                continue;
            }
            if (body != null) {
                requestBody = args[paramIndex];
                continue;
            }
            if (path != null) {
                if (!url.contains("{" + path.value() + "}") || args[paramIndex] == null) {
                    throw new IllegalArgumentException("Path variable " + path.value() + " is not provided");
                }
                Object pathVar = args[paramIndex];
                if (pathVar instanceof String) {
                    url = url.replace("{" + path.value() + "}", args[paramIndex].toString());
                } else if (ClassTypeAndMethods.isBoxedType(pathVar.getClass()) || ClassTypeAndMethods.isBasicType(pathVar.getClass())) {
                    Converter converter = converters.getConverter(pathVar.getClass(), String.class);
                    url = url.replace("{" + path.value() + "}", (String)converter.convert(args[paramIndex]));
                }
            }
            if(header != null) {
                headersMap.put(header.key(), args[paramIndex].toString());
            }
        }

        url = url.replaceAll("/+", "/");
        if (baseUrl.endsWith("/")) {
            url = baseUrl.substring(0, baseUrl.length() - 1) + url;
        } else {
            url = baseUrl + url;
        }

        if (provider != null) {
            provider.authenticate(headersMap);
        }

        if (method.getReturnType() == NetworkAsync.class) {
            return requester.executeAsync(endPoint.method(), url, headersMap, requestBody);
        }

        InputStream is = requester.execute(endPoint.method(), url, headersMap, requestBody);
        if (is == null) {
            return null;
        }
        Object returnValue = null;
        if (method.getReturnType() == InputStream.class) {
            returnValue = new ByteArrayInputStream(is.readAllBytes());
        } else if (method.getReturnType() == String.class) {
            returnValue = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } else if(ClassTypeAndMethods.isBasicType(method.getReturnType()) || ClassTypeAndMethods.isBoxedType(method.getReturnType())) {
            Converter converter = converters.getConverter(String.class, method.getReturnType());
            String value = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            returnValue = converter.convert(value);
        } else if (method.getReturnType() == byte[].class) {
            returnValue = is.readAllBytes();
        } else if (method.getReturnType() == void.class) {
            returnValue = null;
        } else {
            returnValue = mapper.readValue(is, method.getReturnType());
        }
        is.close();
        return returnValue;
    }


    public static <T> T create(Class<T> networkInterface, String baseUrl, AuthProvider provider) {
        return create(networkInterface, baseUrl, provider, new DefaultRequester());
    }

    public static <T> T create(Class<T> networkInterface, String baseUrl, AuthProvider provider, HttpRequester requester) {

        if (baseUrl == null || !baseUrl.toLowerCase().startsWith("http")) {
            throw new IllegalArgumentException("baseUrl must be a valid http url");
        }

        if (!networkInterface.isInterface()) {
            throw new IllegalArgumentException("Parameter must be a interface");
        }

        TypedKey<T> key = TypedKey.getTypedKey(networkInterface, baseUrl);
        if (instances.contains(key)) {
            return (T) instances.get(key);
        }
        Network network = new Network(provider,requester,baseUrl);
        T client = (T) Proxy.newProxyInstance(
                Network.class.getClassLoader(),
                new Class<?>[]{networkInterface},
                network
        );
        instances.put(key, client);
        return client;
    }

}
