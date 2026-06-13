package dev.knoxy.zexreflection.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Intercepts orchestration workflows to apply raw field writes directly to structural variables 
 * without routing through pipeline validation filters.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ZFieldSetNotProcess {
}