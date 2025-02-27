package com.binaryigor.htmxvsreact.shared.html;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class HTMLTemplates {

    private final TemplateFactory factory;
    private final HTMLConfig htmlConfig;
    private final Translations translations;

    public HTMLTemplates(TemplateFactory factory, HTMLConfig htmlConfig, Translations translations) {
        this.factory = factory;
        this.htmlConfig = htmlConfig;
        this.translations = translations;
    }

    public String renderPage(String template, Map<String, Object> params) {
        return render(template, params, false);
    }

    public String render(String template, Map<String, Object> params) {
        return render(template, params, true);
    }

    private String render(String template, Map<String, ?> params, boolean partial) {
        try {
            var compiled = doRender(template, params);
            if (partial) {
                return compiled;
            }
            return doRender("page-skeleton.mustache", enrichedForFullPageParams(params, compiled));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String doRender(String template, Map<String, ?> params) {
        try {
            var compiled = factory.compile("static/templates/" + template);
            var writer = new StringWriter();
            compiled.execute(writer, params).flush();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, ?> enrichedForFullPageParams(Map<String, ?> params, String content) {
        var enrichedParams = new HashMap<String, Object>(params);
        enrichedParams.put("cssPath", htmlConfig.cssPath());
        enrichedParams.put("htmxPath", htmlConfig.htmxPath());
        enrichedParams.put("content", content);
        enrichedParams.put("errorModalTitle", translations.message("error-modal-title"));
        return translations.enrich(enrichedParams, "navigation");
    }
}
