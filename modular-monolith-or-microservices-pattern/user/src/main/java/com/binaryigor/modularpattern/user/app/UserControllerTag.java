package com.binaryigor.modularpattern.user.app;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Tag(name = "User Module/Service")
public @interface UserControllerTag {
}
