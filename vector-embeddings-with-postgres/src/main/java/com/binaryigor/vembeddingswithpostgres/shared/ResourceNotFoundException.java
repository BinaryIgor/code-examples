package com.binaryigor.vembeddingswithpostgres.shared;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException ofType(String type, String id) {
        return new ResourceNotFoundException("Resource %s of %s id was not found".formatted(type, id));
    }
}
