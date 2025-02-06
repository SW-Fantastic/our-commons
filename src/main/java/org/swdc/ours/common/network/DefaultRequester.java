package org.swdc.ours.common.network;

import org.swdc.ours.common.type.ClassTypeAndMethods;
import org.swdc.ours.common.type.Converter;
import org.swdc.ours.common.type.Converters;
import org.swdc.ours.common.type.JSONMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 *
 */
public class DefaultRequester implements HttpRequester {


    private static Converters converters = new Converters();


    @Override
    public InputStream execute(Methods method, String url, Map<String, String> headers, Object requestBody) throws Exception {
        URL theUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) theUrl.openConnection();
        connection.setRequestMethod(method.name());
        connection.setDoInput(true);
        connection.setDoOutput(true);

        for (Map.Entry<String,String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        connection.connect();
        if(requestBody != null) {
            OutputStream os = connection.getOutputStream();
            if (requestBody instanceof String) {
                os.write(((String) requestBody).getBytes(StandardCharsets.UTF_8));
            } else if (requestBody instanceof InputStream) {
                InputStream is = (InputStream) requestBody;
                is.transferTo(os);
            } else if (requestBody instanceof File) {
                File file = (File) requestBody;
                if (file.exists()) {
                    FileInputStream fis = new FileInputStream(file);
                    fis.transferTo(os);
                    fis.close();
                }
            } else {
                byte[] data = JSONMapper.writeBytes(requestBody);
                os.write(data);
            }
            os.flush();
            os.close();
        }

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream is = connection.getInputStream();
            ByteArrayInputStream bin = new ByteArrayInputStream(is.readAllBytes());
            is.close();
            connection.disconnect();
            return bin;
        }

        connection.disconnect();

        throw new NetworkException(connection.getResponseCode());
    }

    @Override
    public NetworkAsync executeAsync(Methods method, String url, Map<String, String> headers, Object requestBody) throws Exception {

       return new NetworkAsync((progress, bodyHandler)->{
            try {
                URL theUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) theUrl.openConnection();
                connection.setRequestMethod(method.name());
                connection.setDoInput(true);
                connection.setDoOutput(true);

                for (Map.Entry<String,String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }

                connection.connect();
                if(requestBody != null) {

                    byte[] buffer = new byte[1024 * 1024];
                    InputStream is = null;
                    OutputStream os = connection.getOutputStream();
                    if (requestBody instanceof String) {
                        String body = (String) requestBody;
                        byte[] data = body.getBytes(StandardCharsets.UTF_8);
                        is = new ByteArrayInputStream(data);
                    } else if (requestBody instanceof InputStream) {
                        is = (InputStream) requestBody;
                    } else if (requestBody instanceof File) {
                        File file = (File) requestBody;
                        if (file.exists()) {
                            is = new FileInputStream(file);
                        } else {
                            is = new ByteArrayInputStream(new byte[0]);
                        }
                    } else {
                        byte[] data = JSONMapper.writeBytes(requestBody);
                        if (data == null) {
                            is = new ByteArrayInputStream(new byte[0]);
                        } else {
                            is = new ByteArrayInputStream(data);
                        }
                    }
                    long total = is.available();
                    long current = 0;
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                        current += read;
                        if (progress != null) {
                            progress.onProgress(NetworkDirection.SEND, current, total);
                        }
                    }
                    os.flush();
                    os.close();
                    is.close();

                }

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = connection.getInputStream();
                    byte[] buffer = new byte[1024 * 1024];
                    long total = is.available();
                    long current = 0;
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        bodyHandler.accept(buffer);
                        current += read;
                        if (progress != null) {
                            progress.onProgress(NetworkDirection.RECEIVE, current, total);
                        }
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

}
