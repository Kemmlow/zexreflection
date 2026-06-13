package dev.knoxy.zexreflection.annotation;

import androidx.annotation.NonNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a reflection proxy interface to a target class using its fully qualified binary string name.
 * Ideal for wrapping hidden or unlinked compile-time platform system components.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ZClassName {
    /**
     * @return The fully qualified binary name of the target class (e.g., "android.os.ServiceManager").
     */
    @NonNull String value();
}