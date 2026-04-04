package com.binaryigor.complexity_alternative.app;

import com.samskivert.mustache.Mustache;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.List;

@Component
public class TemplatesResolver {

    private final Mustache.Compiler compiler;
    private final String cssPath;
    private final String htmxPath;
    private final List<String> componentPaths;

    public TemplatesResolver(Mustache.Compiler compiler, WebProperties properties) {
        this.compiler = compiler;
        this.cssPath = properties.cssPath();
        this.htmxPath = properties.htmxPath();
        this.componentPaths = properties.componentPaths();
    }

    public String resolve(String template, Model model) {
        if (HTMX.isHTMXRequest()) {
            return template;
        }

        model.addAttribute("cssPath", cssPath)
                .addAttribute("htmxPath", htmxPath)
                .addAttribute("componentPaths", componentPaths)
                .addAttribute(template, true);

        return "page-wrapper";
    }

    public String render(String template, Model model, boolean fragment) {
        model.addAttribute("cssPath", cssPath)
                .addAttribute("htmxPath", htmxPath)
                .addAttribute("componentPaths", componentPaths);

        if (fragment) {
            return compiler.loadTemplate(template).execute(model);
        }

        model.addAttribute(template, true);

        return compiler.loadTemplate("page-wrapper").execute(model);
    }
}
