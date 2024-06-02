package com.binaryigor.modularpattern.project.app;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Tag(name = "Project Module/Service")
public @interface ProjectControllerTag {
}
