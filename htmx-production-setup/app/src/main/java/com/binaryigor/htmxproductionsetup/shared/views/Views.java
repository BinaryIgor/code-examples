package com.binaryigor.htmxproductionsetup.shared.views;

public class Views {

    public static String inputError(String error) {
        return "<input-error%s></input-error>".formatted(error == null || error.isBlank() ? "" :
                " message=\"%s\"".formatted(error));
    }
}
