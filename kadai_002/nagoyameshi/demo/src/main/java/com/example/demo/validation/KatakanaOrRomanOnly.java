package com.example.demo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = KatakanaOrRomanOnlyValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface KatakanaOrRomanOnly {
    String message() default "name for reservation can only contain roman or katakana.";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default{};
}
