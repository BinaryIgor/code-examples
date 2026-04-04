package com.binaryigor.complexity_alternative.app;

import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.Locale;
import java.util.Map;

@Component
public class Translations {

    private final MessageSource messageSource;

    public Translations(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void enrich(Model model, Locale locale, String... keys) {
        enrich(model, locale, Map.of(), Map.of(), keys);
    }

    public void enrich(Model model, Locale locale, Map<String, String> mapping, String... keys) {
        enrich(model, locale, mapping, Map.of(), keys);
    }

    public void enrich(Model model, Locale locale,
                       Map<String, String> mapping,
                       Map<String, Map<String, String>> keysToVariables,
                       String... keys) {
        for (var k : keys) {
            var t = messageSource.getMessage(k, null, locale);
            var tVars = keysToVariables.getOrDefault(k, Map.of());
            if (!tVars.isEmpty()) {
                t = replaceTranslationVars(t, tVars);
            }

            var attribute = mapping.getOrDefault(k, k);
            model.addAttribute(attribute, t);
            if (!attribute.equals(k)) {
                model.addAttribute(k, t);
            }
        }
    }

    private String replaceTranslationVars(String template, Map<String, String> vars) {
        var t = template;
        for (var e : vars.entrySet()) {
            t = t.replace(e.getKey(), e.getValue());
        }
        return t;
    }

    public String translate(String key, @Nullable String defaultMessage, Locale locale) {
        return messageSource.getMessage(key, null, defaultMessage, locale);
    }
}
