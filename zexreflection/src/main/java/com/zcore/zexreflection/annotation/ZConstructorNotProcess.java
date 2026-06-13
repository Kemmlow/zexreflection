package com.zcore.zexreflection.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allocates a raw constructor structural allocation invocation while explicitly bypassing code-generation optimization validation tasks.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ZConstructorNotProcess {
}