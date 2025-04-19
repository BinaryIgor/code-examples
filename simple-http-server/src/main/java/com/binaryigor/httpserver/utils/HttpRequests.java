package com.binaryigor.httpserver.utils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HttpRequests {

    public static Map<String, String> parseUrlEncodedForm(byte[] rawForm, Charset charset) {
        var form = new HashMap<String, String>();
        return form;
    }

    public static Map<String, String> parseQueryParams(String url, Charset charset) {
        var params = new HashMap<String, String>();
        return params;
    }
}
