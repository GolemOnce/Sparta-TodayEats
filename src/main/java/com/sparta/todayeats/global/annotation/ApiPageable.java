package com.sparta.todayeats.global.annotation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Parameters({
        @Parameter(name = "page", description = "페이지 번호", example = "0"),
        @Parameter(name = "size", description = "페이지 크기 (10, 30, 50)", example = "10"),
        @Parameter(name = "sort", description = "정렬", example = "createdAt,desc")
})
public @interface ApiPageable {
}