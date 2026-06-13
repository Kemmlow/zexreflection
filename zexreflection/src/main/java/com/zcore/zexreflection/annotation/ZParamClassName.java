package com.zcore.zexreflection.annotation;

import androidx.annotation.NonNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides the signature resolution of a proxy method parameter using a string name representation.
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ZParamClassName {
    /**
     * @return The fully qualified binary name string representing the signature target class.
     */
    @NonNull String value();
}