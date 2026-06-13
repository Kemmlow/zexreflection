package dev.knoxy.zexreflection.annotation;

import androidx.annotation.NonNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a configuration class name string while explicitly instructing pre-compilation 
 * annotation processing tools to bypass optimization structural parsing passes.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ZClassNameNotProcess {
    /**
     * @return The fully qualified binary name of the target class.
     */
    @NonNull String value();
}