package dev.knoxy.zexreflection.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Unwraps an operation to return the raw underlying reflection {@link java.lang.reflect.Field} reference object 
 * directly, bypassing value collection routines.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ZFieldCheckNotProcess {
}