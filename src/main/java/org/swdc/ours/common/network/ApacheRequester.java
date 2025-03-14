package org.swdc.ours.common.network;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.client.methods.*;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.swdc.ours.common.helper.ListenableInputStream;
import org.swdc.ours.common.helper.ProgressDirection;
import org.swdc.ours.common.type.ClassTypeAndMethods;
import org.swdc.ours.common.type.Converter;
import org.swdc.ours.common.type.Converters;
import org.swdc.ours.common.type.JSONMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Apache请求器，封装了Apache HttpClient的使用。
 */
public class ApacheRequester implements HttpRequester {

    private static final Converters converters = new Converters();

    private HttpClient client = HttpClientBuilder
            .create()
            .build();

    /**
     * 执行请求，返回输入流。
     * @param method HTTP方法，例如GET、POST等。
     * @param url 请求的URL。
     * @param headers 请求头信息。
     * @param body 请求体信息。
     * @return 输入流，如果请求失败返回null。
     * @throws Exception   如果请求失败，抛出异常。
     */
    @Override
    public InputStream execute(Methods method, String url, Map<String, String> headers, Object body) throws Exception {

        // 创建请求对象
        HttpUriRequest request = createRequest(method,url,headers);
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if (!request.containsHeader(header.getKey())) {
                request.addHeader(header.getKey(), header.getValue());
            }
        }

        if (body != null && request instanceof HttpEntityEnclosingRequestBase) {
            // 当存在请求体的时候，设置请求体
            HttpEntityEnclosingRequestBase entityRequest = (HttpEntityEnclosingRequestBase) request;
            HttpEntity entity = null;
            if (body instanceof File) {
                // 文件请求体
                entity = new FileEntity((File) body);
            } else if(body instanceof InputStream) {
                // 流请求体
                entity = new InputStreamEntity((InputStream) body);
            } else if (ClassTypeAndMethods.isBoxedType(body.getClass()) || ClassTypeAndMethods.isBasicType(body.getClass())) {
                // 基本类型请求体
                Converter converter = converters.getConverter(body.getClass(), String.class);
                entity = new StringEntity((String) converter.convert(body), StandardCharsets.UTF_8);
            } else if (body instanceof String) {
                // String请求体
                entity = new StringEntity((String) body);
            } else {
                // Pojo请求体
                String data = JSONMapper.writeString(body);
                if (data == null) {
                    throw new IllegalArgumentException("Cannot convert body to JSON");
                }
                entity = new StringEntity(data, StandardCharsets.UTF_8);
            }
            entityRequest.setEntity(entity);
        }

        // 执行请求
        HttpResponse response = client.execute(request);
        if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300) {
            if (response.getEntity() != null) {
                return response.getEntity().getContent();
            }
            return null;
        } else if (response.getStatusLine().getStatusCode() == 404) {
            return null;
        }

        throw new NetworkException(response.getStatusLine().getStatusCode());
    }

    /**
     * 执行异步请求，返回一个NetworkAsync对象。
     * @param method HTTP方法，例如GET、POST等。
     * @param url 请求的URL。
     * @param headers 请求头信息。
     * @param body 请求体信息。
     * @return NetworkAsync对象，可以用来监听进度和获取响应。
     * @throws Exception 如果请求失败，抛出异常。
     */
    @Override
    public NetworkAsync executeAsync(Methods method, String url, Map<String, String> headers, Object body) throws Exception {
        return new NetworkAsync((progressListener, bodyHandler) -> {
            try {
                HttpUriRequest request = createRequest(method,url,headers);
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    if (!request.containsHeader(header.getKey())) {
                        request.addHeader(header.getKey(), header.getValue());
                    }
                }

                if (body != null && request instanceof HttpEntityEnclosingRequestBase) {
                    HttpEntityEnclosingRequestBase entityRequest = (HttpEntityEnclosingRequestBase) request;
                    HttpEntity entity = null;
                    if (body instanceof File) {
                        File file = (File) body;
                        ListenableInputStream is = new ListenableInputStream(new FileInputStream(file), progressListener);
                        entity = new InputStreamEntity(is,file.length());
                    } else if(body instanceof InputStream) {
                        ListenableInputStream is = new ListenableInputStream((InputStream) body, progressListener);
                        entity = new InputStreamEntity(is,is.available());
                    } else if (ClassTypeAndMethods.isBoxedType(body.getClass()) || ClassTypeAndMethods.isBasicType(body.getClass())) {
                        Converter converter = converters.getConverter(body.getClass(), String.class);
                        entity = new StringEntity((String) converter.convert(body));
                    } else if (body instanceof String) {
                        entity = new StringEntity((String) body);
                    } else {
                        String data = JSONMapper.writeString(body);
                        if (data == null) {
                            throw new IllegalArgumentException("Cannot convert body to JSON");
                        }
                        entity = new StringEntity(data);
                    }
                    entityRequest.setEntity(entity);
                }
                HttpResponse response = client.execute(request);
                if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300) {
                    if (response.getEntity() == null) {
                        progressListener.onProgress(ProgressDirection.WRITE, 1, 1);
                        return;
                    }
                    InputStream is = response.getEntity().getContent();

                    byte[] buffer = new byte[1024 * 1024];
                    long total = is.available();
                    long current = 0;
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        try {
                            bodyHandler.handle(buffer, read);
                            current += read;
                            if (progressListener != null) {
                                progressListener.onProgress(ProgressDirection.WRITE, current, total);
                            }
                        } catch (CancelledException e) {
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 创建请求对象
     * @param method HTTP方法，例如GET、POST等。
     * @param url 请求的URL。
     * @param headers 请求头信息。
     * @return  HttpUriRequest对象，可以用来执行请求。
     * @throws IOException 如果创建请求失败，抛出异常。
     */
    private HttpUriRequest createRequest(Methods method, String url, Map<String,String> headers) throws IOException {
        switch (method) {
            case GET:
                return new HttpGet(url);
            case PUT:
                return new HttpPut(url);
            case POST:
                return new HttpPost(url);
            case DELETE:
                return new HttpDelete(url);
            case PROPFIND:
                if (headers.containsKey("Depth")) {
                    return new HttpPropfind(url, DavConstants.PROPFIND_ALL_PROP, Integer.parseInt(headers.get("Depth")));
                }
                return new HttpPropfind(url, DavConstants.PROPFIND_ALL_PROP, 0);
            case MKCOL:
                return new HttpMkcol(url);
            case COPY:
                if (headers.containsKey("Destination")) {
                    return new HttpCopy(url, headers.get("Destination"),true,false);
                }
                throw new IllegalArgumentException("Destination header is required for COPY");
            case MOVE:
                if (headers.containsKey("Destination")) {
                    return new HttpMove(url, headers.get("Destination"),true);
                }
                throw new IllegalArgumentException("Destination header is required for MOVE");
            case LOCK:
                return new HttpLock(url,new LockInfo(-1));
            case UNLOCK:
                if (headers.containsKey("Lock-Token")) {
                    return new HttpUnlock(url,headers.get("Lock-Token"));
                }
                throw new IllegalArgumentException("Lock-Token header is required for UNLOCK");
        }
        throw new IllegalArgumentException("Unsupported method: " + method);
    }

}
