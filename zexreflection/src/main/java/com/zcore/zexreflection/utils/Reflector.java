/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zcore.zexreflection.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A fluent, type-safe wrapper API for Java Reflection operations.
 */
public class Reflector {
    public static final String LOG_TAG = "Reflector";

    @Nullable protected Object caller;
    @Nullable protected Constructor<?> constructor;
    @Nullable protected Field field;
    @Nullable protected Method method;
    @Nullable protected Class<?> targetType;

    protected Reflector() {
        // Subclassing and factory instantiation control
    }

    @NonNull
    public static Reflector on(@NonNull String className) throws Exception {
        return on(className, true, Reflector.class.getClassLoader());
    }

    @NonNull
    public static Reflector on(@NonNull String className, boolean initialize) throws Exception {
        return on(className, initialize, Reflector.class.getClassLoader());
    }

    @NonNull
    public static Reflector on(@NonNull String className, boolean initialize, @Nullable ClassLoader classLoader) throws Exception {
        try {
            return on(Class.forName(className, initialize, classLoader));
        } catch (Throwable th) {
            throw new Exception("Failed to resolve class: " + className, th);
        }
    }

    @NonNull
    public static Reflector on(@Nullable Class<?> clazz) {
        Reflector reflector = new Reflector();
        reflector.targetType = clazz;
        return reflector;
    }

    @NonNull
    public static Reflector with(@NonNull Object instance) throws Exception {
        return on(instance.getClass()).bind(instance);
    }

    @NonNull
    public Reflector bind(@Nullable Object instance) throws Exception {
        this.caller = verifyInstanceType(instance);
        return this;
    }

    @NonNull
    public Reflector unbind() {
        this.caller = null;
        return this;
    }

    @NonNull
    public Reflector constructor(@NonNull Class<?>... parameterTypes) throws Exception {
        if (targetType == null) throw new Exception("Target type is null");
        try {
            Constructor<?> declaredConstructor = targetType.getDeclaredConstructor(parameterTypes);
            this.constructor = declaredConstructor;
            declaredConstructor.setAccessible(true);
            this.field = null;
            this.method = null;
            return this;
        } catch (Throwable th) {
            throw new Exception("Failed to resolve constructor", th);
        }
    }

    @NonNull
    public Reflector field(@NonNull String name) throws Exception {
        try {
            Field resolvedField = findField(name);
            this.field = resolvedField;
            resolvedField.setAccessible(true);
            this.constructor = null;
            this.method = null;
            return this;
        } catch (Throwable th) {
            throw new Exception("Failed to resolve field: " + name, th);
        }
    }

    @NonNull
    public Reflector method(@NonNull String name, @NonNull Class<?>... parameterTypes) throws Exception {
        try {
            Method resolvedMethod = findMethod(name, parameterTypes);
            this.method = resolvedMethod;
            resolvedMethod.setAccessible(true);
            this.constructor = null;
            this.field = null;
            return this;
        } catch (NoSuchMethodException e) {
            throw new Exception("Method not found: " + name, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <R> R newInstance(@Nullable Object... args) throws Exception {
        if (this.constructor == null) {
            throw new Exception("Constructor targets were not initialized!");
        }
        try {
            return (R) this.constructor.newInstance(args);
        } catch (InvocationTargetException e) {
            throw new Exception("Exception thrown during construction", e.getTargetException());
        } catch (Throwable th) {
            throw new Exception("Construction instantiation failed", th);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <R> R call(@Nullable Object... args) throws Exception {
        return (R) callByCaller(this.caller, args);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <R> R callByCaller(@Nullable Object explicitCaller, @Nullable Object... args) throws Exception {
        validateReflectionState(explicitCaller, this.method, "Method");
        try {
            return (R) this.method.invoke(explicitCaller, args);
        } catch (InvocationTargetException e) {
            throw new Exception("Target method execution failed", e.getTargetException());
        } catch (Throwable th) {
            throw new Exception("Method invocation failed", th);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <R> R get() throws Exception {
        return (R) get(this.caller);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <R> R get(@Nullable Object explicitCaller) throws Exception {
        validateReflectionState(explicitCaller, this.field, "Field");
        try {
            return (R) this.field.get(explicitCaller);
        } catch (Throwable th) {
            throw new Exception("Failed to access field value", th);
        }
    }

    @NonNull
    public Reflector set(@Nullable Object value) throws Exception {
        return set(this.caller, value);
    }

    @NonNull
    public Reflector set(@Nullable Object explicitCaller, @Nullable Object value) throws Exception {
        validateReflectionState(explicitCaller, this.field, "Field");
        try {
            this.field.set(explicitCaller, value);
            return this;
        } catch (Throwable th) {
            throw new Exception("Failed to assign field value", th);
        }
    }

    @Nullable
    public Field getField() {
        return this.field;
    }

    @Nullable
    public Method getMethod() {
        return this.method;
    }

    @NonNull
    protected Field findField(@NonNull String name) throws NoSuchFieldException {
        if (targetType == null) throw new NoSuchFieldException("Target type is null");
        try {
            return targetType.getField(name);
        } catch (NoSuchFieldException e) {
            for (Class<?> current = targetType; current != null; current = current.getSuperclass()) {
                try {
                    return current.getDeclaredField(name);
                } catch (NoSuchFieldException ignored) {}
            }
            throw e;
        }
    }

    @NonNull
    protected Method findMethod(@NonNull String name, @NonNull Class<?>... parameterTypes) throws NoSuchMethodException {
        if (targetType == null) throw new NoSuchMethodException("Target type is null");
        try {
            return targetType.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            for (Class<?> current = targetType; current != null; current = current.getSuperclass()) {
                try {
                    return current.getDeclaredMethod(name, parameterTypes);
                } catch (NoSuchMethodException ignored) {}
            }
            throw e;
        }
    }

    protected void validateReflectionState(@Nullable Object instance, @Nullable Member member, @NonNull String typeLabel) throws Exception {
        if (member == null) {
            throw new Exception(typeLabel + " was null!");
        }
        if (instance == null && !Modifier.isStatic(member.getModifiers())) {
            throw new Exception("Instance required for non-static structural references!");
        }
        verifyInstanceType(instance);
    }

    @Nullable
    protected Object verifyInstanceType(@Nullable Object instance) throws Exception {
        if (instance == null || (targetType != null && targetType.isInstance(instance))) {
            return instance;
        }
        throw new Exception("Caller [" + instance + "] is not an instance of target class [" + targetType + "]!");
    }

    /**
     * A reflection context implementation that silences exceptions internally.
     */
    public static final class QuietReflector extends Reflector {
        @Nullable private Throwable ignoredException;

        @NonNull
        public static QuietReflector on(@NonNull String className) {
            return on(className, true, QuietReflector.class.getClassLoader());
        }

        @NonNull
        public static QuietReflector on(@NonNull String className, boolean initialize) {
            return on(className, initialize, QuietReflector.class.getClassLoader());
        }

        @NonNull
        public static QuietReflector on(@NonNull String className, boolean initialize, @Nullable ClassLoader classLoader) {
            Class<?> resolvedClass = null;
            try {
                resolvedClass = Class.forName(className, initialize, classLoader);
                return on(resolvedClass, null);
            } catch (Throwable th) {
                return on(resolvedClass, th);
            }
        }

        @NonNull
        public static QuietReflector on(@Nullable Class<?> clazz) {
            return on(clazz, clazz == null ? new Exception("Target type context definition was null!") : null);
        }

        @NonNull
        private static QuietReflector on(@Nullable Class<?> clazz, @Nullable Throwable contextException) {
            QuietReflector quietReflector = new QuietReflector();
            quietReflector.targetType = clazz;
            quietReflector.ignoredException = contextException;
            return quietReflector;
        }

        @NonNull
        public static QuietReflector with(@Nullable Object instance) {
            return instance == null ? on((Class<?>) null) : on(instance.getClass()).bind(instance);
        }

        @Override
        @NonNull
        public QuietReflector bind(@Nullable Object instance) {
            if (shouldSkipAlways()) return this;
            try {
                this.ignoredException = null;
                super.bind(instance);
            } catch (Throwable th) {
                this.ignoredException = th;
            }
            return this;
        }

        @Override
        @NonNull
        public QuietReflector unbind() {
            super.unbind();
            return this;
        }

        @Override
        @NonNull
        public QuietReflector constructor(@NonNull Class<?>... parameterTypes) {
            if (shouldSkipAlways()) return this;
            try {
                this.ignoredException = null;
                super.constructor(parameterTypes);
            } catch (Throwable th) {
                this.ignoredException = th;
            }
            return this;
        }

        @Override
        @NonNull
        public QuietReflector field(@NonNull String name) {
            if (shouldSkipAlways()) return this;
            try {
                this.ignoredException = null;
                super.field(name);
            } catch (Throwable th) {
                this.ignoredException = th;
            }
            return this;
        }

        @Override
        @NonNull
        public QuietReflector method(@NonNull String name, @NonNull Class<?>... parameterTypes) {
            if (shouldSkipAlways()) return this;
            try {
                this.ignoredException = null;
                super.method(name, parameterTypes);
            } catch (Throwable th) {
                this.ignoredException = th;
            }
            return this;
        }

        @Override
        @Nullable
        public <R> R newInstance(@Nullable Object... args) {
            if (shouldSkipExecution()) return null;
            try {
                this.ignoredException = null;
                return super.newInstance(args);
            } catch (Throwable th) {
                this.ignoredException = th;
                return null;
            }
        }

        @Override
        @Nullable
        public <R> R call(@Nullable Object... args) {
            if (shouldSkipExecution()) return null;
            try {
                this.ignoredException = null;
                return super.call(args);
            } catch (Throwable th) {
                this.ignoredException = th;
                return null;
            }
        }

        @Override
        @Nullable
        public <R> R callByCaller(@Nullable Object explicitCaller, @Nullable Object... args) {
            if (shouldSkipExecution()) return null;
            try {
                this.ignoredException = null;
                return super.callByCaller(explicitCaller, args);
            } catch (Throwable th) {
                this.ignoredException = th;
                return null;
            }
        }

        @Override
        @Nullable
        public <R> R get() {
            if (shouldSkipExecution()) return null;
            try {
                this.ignoredException = null;
                return super.get();
            } catch (Throwable th) {
                this.ignoredException = th;
                return null;
            }
        }

        @Override
        @Nullable
        public <R> R get(@Nullable Object explicitCaller) {
            if (shouldSkipExecution()) return null;
            try {
                this.ignoredException = null;
                return super.get(explicitCaller);
            } catch (Throwable th) {
                this.ignoredException = th;
                return null;
            }
        }

        @Override
        @NonNull
        public QuietReflector set(@Nullable Object value) {
            if (shouldSkipExecution()) return this;
            try {
                this.ignoredException = null;
                super.set(value);
            } catch (Throwable th) {
                this.ignoredException = th;
            }
            return this;
        }

        @Override
        @NonNull
        public QuietReflector set(@Nullable Object explicitCaller, @Nullable Object value) {
            if (shouldSkipExecution()) return this;
            try {
                this.ignoredException = null;
                super.set(explicitCaller, value);
            } catch (Throwable th) {
                this.ignoredException = th;
            }
            return this;
        }

        public boolean shouldSkipExecution() {
            return shouldSkipAlways() || this.ignoredException != null;
        }

        public boolean shouldSkipAlways() {
            return this.targetType == null;
        }

        @Nullable
        public Throwable getIgnoredException() {
            return this.ignoredException;
        }
    }
}