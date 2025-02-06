package org.swdc.ours.common.network;

import java.io.InputStream;
import java.util.Map;

public interface HttpRequester {

    InputStream execute(Methods method, String url, Map<String, String> headers, Object body) throws Exception;

    NetworkAsync executeAsync(Methods method, String url, Map<String, String> headers, Object body) throws Exception;

}
