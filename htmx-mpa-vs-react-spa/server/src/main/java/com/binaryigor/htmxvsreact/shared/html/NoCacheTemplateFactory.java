package com.binaryigor.htmxvsreact.shared.html;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.util.function.Supplier;

public class NoCacheTemplateFactory implements TemplateFactory {

    private final Supplier<MustacheFactory> mustacheFactorySupplier;

    public NoCacheTemplateFactory(Supplier<MustacheFactory> mustacheFactorySupplier) {
        this.mustacheFactorySupplier = mustacheFactorySupplier;
    }

    @Override
    public Mustache compile(String name) {
        return mustacheFactorySupplier.get().compile(name);
    }
}
