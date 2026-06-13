package com.zcore.zexreflection.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Acts as a standard instance field value reader while instructing annotation processing engines 
 * to skip downstream optimization or validation operations.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ZFieldNotProcess {
}