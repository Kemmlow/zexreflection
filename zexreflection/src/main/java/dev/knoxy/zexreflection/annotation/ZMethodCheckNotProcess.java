package dev.knoxy.zexreflection.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Unwraps an operation to return the raw underlying reflection {@link java.lang.reflect.Method} reference object 
 * directly, bypassing standard execution triggers.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ZMethodCheckNotProcess {
}