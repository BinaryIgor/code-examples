package com.binaryigor.httpserver.utils;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HttpRequests {

    public static Map<String, String> parseUrlEncodedForm(byte[] rawForm, Charset charset) {
        var keyValues = URLDecoder.decode(new String(rawForm, charset), charset).split("&");
        var form = new HashMap<String, String>();
        for (var kv : keyValues) {
            var keyValue = kv.split("=", 2);
            form.put(keyValue[0].strip(), keyValue.length == 1 ? "" : keyValue[1].strip());
        }
        return form;
    }

    public static Map<String, String> parseQueryParams(String url, Charset charset) {
        System.out.println("Url to decode: " + url);
        var startIdx = url.indexOf("?");
        var keyValues = URLDecoder.decode(startIdx >= 0 ? url.substring(startIdx + 1) : url, charset).split("&");
        var params = new HashMap<String, String>();
        for (var kv : keyValues) {
            var keyValue = kv.split("=", 2);
            var key = keyValue[0].strip();
            var value = keyValue.length == 1 ? "" : keyValue[1].strip();
            params.merge(key, value, (a, b) -> a + "," + b);
        }
        return params;
    }
}
