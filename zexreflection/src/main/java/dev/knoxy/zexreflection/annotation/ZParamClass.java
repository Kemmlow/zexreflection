package dev.knoxy.zexreflection.annotation;

import androidx.annotation.NonNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides the signature resolution of a proxy method parameter using an explicit class token.
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ZParamClass {
    /**
     * @return The explicit type token representing the signature target class.
     */
    @NonNull Class<?> value();
}