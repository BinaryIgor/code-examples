package com.binaryigor.htmxvsreact.html;

import com.binaryigor.htmxvsreact.shared.Translations;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class HTMLTemplates {

    private final TemplateFactory factory;
    private final HTMLConfig htmlConfig;

    public HTMLTemplates(TemplateFactory factory, HTMLConfig htmlConfig) {
        this.factory = factory;
        this.htmlConfig = htmlConfig;
    }

    public String renderPage(String template, Map<String, Object> params) {
        return render(template, params, false);
    }

    public String render(String template, Map<String, Object> params) {
        return render(template, params, true);
    }

    private String render(String template, Map<String, Object> params, boolean partial) {
        try {
            var withTranslationsParams = enrichedWithTranslationsParams(params);
            var compiled = doRender(template, withTranslationsParams);
            if (partial) {
                return compiled;
            }
            return doRender("page-skeleton.mustache", enrichedWithAssetsParams(withTranslationsParams, compiled));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String doRender(String template, Map<String, Object> params) {
        try {
            var compiled = factory.compile("static/templates/" + template);
            var writer = new StringWriter();
            compiled.execute(writer, params).flush();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> enrichedWithAssetsParams(Map<String, Object> params, String content) {
        var enrichedParams = new HashMap<>(params);
        enrichedParams.put("cssPath", htmlConfig.cssPath());
        enrichedParams.put("htmxPath", htmlConfig.htmxPath());
        enrichedParams.put("content", content);
        return enrichedParams;
    }

    private Map<String, Object> enrichedWithTranslationsParams(Map<String, Object> params) {
        var enrichedParams = new HashMap<>(params);
        Translations.copyAll(enrichedParams);
        return enrichedParams;
    }

}
