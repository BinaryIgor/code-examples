package com.binaryigor.httpserver.utils;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HttpRequests {

    public static Map<String, String> parseUrlEncodedForm(byte[] formBytes, Charset charset) {
        var decodedForm = URLDecoder.decode(new String(formBytes, charset), charset);
        return parseUrlParams(decodedForm);
    }

    private static Map<String, String> parseUrlParams(String decodedUrl) {
        var params = new HashMap<String, String>();
        for (var keyValuePair : decodedUrl.split("&")) {
            var keyValue = keyValuePair.split("=", 2);
            var key = keyValue[0].strip();
            var value = keyValue.length == 1 ? "" : keyValue[1].strip();
            params.merge(key, value, (p, n) -> p + "," + n);
        }
        return params;
    }

    public static Map<String, String> parseQueryParams(String url, Charset charset) {
        var paramsStartIdx = url.indexOf("?") + 1;
        var decodedUrl = URLDecoder.decode(paramsStartIdx > 0 ? url.substring(paramsStartIdx) : url, charset);
        return parseUrlParams(decodedUrl);
    }
}
