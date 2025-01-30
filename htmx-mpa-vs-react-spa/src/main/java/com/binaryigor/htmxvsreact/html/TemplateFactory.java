package com.binaryigor.htmxvsreact.html;

import com.github.mustachejava.Mustache;

public interface TemplateFactory {
    Mustache compile(String name);
}
