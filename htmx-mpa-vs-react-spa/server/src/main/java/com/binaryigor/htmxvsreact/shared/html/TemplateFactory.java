package com.binaryigor.htmxvsreact.shared.html;

import com.github.mustachejava.Mustache;

public interface TemplateFactory {
    Mustache compile(String name);
}
