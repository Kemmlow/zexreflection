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

package com.zcore.zexreflection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zcore.zexreflection.annotation.ZClass;
import com.zcore.zexreflection.annotation.ZClassName;
import com.zcore.zexreflection.annotation.ZClassNameNotProcess;
import com.zcore.zexreflection.annotation.ZConstructor;
import com.zcore.zexreflection.annotation.ZConstructorNotProcess;
import com.zcore.zexreflection.annotation.ZField;
import com.zcore.zexreflection.annotation.ZFieldCheckNotProcess;
import com.zcore.zexreflection.annotation.ZFieldNotProcess;
import com.zcore.zexreflection.annotation.ZFieldSetNotProcess;
import com.zcore.zexreflection.annotation.ZMethodCheckNotProcess;
import com.zcore.zexreflection.annotation.ZParamClass;
import com.zcore.zexreflection.annotation.ZParamClassName;
import com.zcore.zexreflection.utils.Reflector;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The core orchestration engine of the ZexReflection library.
 * <p>
 * This engine constructs lightweight dynamic proxy interfaces backed by metadata 
 * annotations, mapping structural method patterns securely to low-level targeted reflections.
 */
public final class ZexReflection {
    
    public static boolean IS_CACHE_ENABLED = false;
    public static boolean IS_DEBUG_MODE = false;

    private static final Map<Class<?>, Object> STATIC_PROXY_CACHE = new HashMap<>();
    private static final Map<Class<?>, Object> STATIC_PROXY_EXCEPTION_CACHE = new HashMap<>();
    private static final WeakHashMap<Object, Map<Class<?>, Object>> INSTANCE_PROXY_REGISTRY = new WeakHashMap<>();

    private ZexReflection() {
        // Core framework orchestration utility
    }

    /**
     * Creates a high-performance declarative reflection mapping instance wrapping a targeted interface class configuration.
     *
     * @param proxyInterface   The declaration interface blueprint containing reflection structural definitions.
     * @param targetInstance   The explicit runtime target object reference instance (null if tracking static elements).
     * @param throwOnFailure   If true, internal failure sequences bubble up natively; if false, failures fail silently returning safe mock tokens.
     * @return A dynamic proxy implementing {@code proxyInterface}.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T create(@NonNull Class<T> proxyInterface, @Nullable Object targetInstance, final boolean throwOnFailure) {
        try {
            // Attempt to resolve existing cached proxy configurations
            T cachedProxy = getCachedProxy(proxyInterface, targetInstance, throwOnFailure);
            if (cachedProxy != null) {
                return cachedProxy;
            }

            final WeakReference<Object> targetRef = targetInstance == null ? null : new WeakReference<>(targetInstance);
            final Class<?> targetReflectionClass = resolveTargetReflectionClass(proxyInterface);

            T dynamicProxy = (T) Proxy.newProxyInstance(
                    proxyInterface.getClassLoader(),
                    new Class[]{proxyInterface},
                    new InvocationHandler() {
                        @Override
                        @Nullable
                        public Object invoke(@NonNull Object proxy, @NonNull Method method, @Nullable Object[] args) throws Throwable {
                            String methodName = method.getName();
                            Class<?> returnType = method.getReturnType();

                            try {
                                boolean isStaticContext = (targetRef == null);
                                Object liveInstance = isStaticContext ? null : targetRef.get();

                                // 1. Check Field Modification Mappings (@ZFieldSetNotProcess)
                                if (method.isAnnotationPresent(ZFieldSetNotProcess.class)) {
                                    // Strips structural setter action verbs (e.g., stripping customized intercept labels)
                                    Reflector fieldSetter = Reflector.on(targetReflectionClass).field(methodName.substring(5));
                                    if (isStaticContext) {
                                        fieldSetter.set(args != null ? args[0] : null);
                                    } else {
                                        if (liveInstance == null) return handleNullValueFallback(returnType);
                                        fieldSetter.set(liveInstance, args != null ? args[0] : null);
                                    }
                                    return handleNullValueFallback(returnType);
                                }

                                // 2. Check Raw Structural Field Reference Accessors (@ZFieldCheckNotProcess)
                                if (method.isAnnotationPresent(ZFieldCheckNotProcess.class)) {
                                    try {
                                        return Reflector.on(targetReflectionClass).field(methodName.substring(7)).getField();
                                    } catch (Throwable th) {
                                        return null;
                                    }
                                }

                                Class<?>[] parameterTypes = resolveParameterTypes(method);

                                // 3. Check Raw Structural Method Reference Accessors (@ZMethodCheckNotProcess)
                                if (method.isAnnotationPresent(ZMethodCheckNotProcess.class)) {
                                    try {
                                        return Reflector.on(targetReflectionClass).method(methodName.substring(7), parameterTypes).getMethod();
                                    } catch (Throwable th) {
                                        return null;
                                    }
                                }

                                // 4. Check Construction Allocation Triggers (@ZConstructor, @ZConstructorNotProcess)
                                if (method.isAnnotationPresent(ZConstructor.class) || method.isAnnotationPresent(ZConstructorNotProcess.class)) {
                                    return Reflector.on(targetReflectionClass).constructor(parameterTypes).newInstance(args);
                                }

                                // 5. Check Standard Field Value Readers (@ZField, @ZFieldNotProcess)
                                if (method.isAnnotationPresent(ZField.class) || method.isAnnotationPresent(ZFieldNotProcess.class)) {
                                    Reflector fieldGetter = Reflector.on(targetReflectionClass).field(methodName);
                                    if (isStaticContext) {
                                        return fieldGetter.get();
                                    }
                                    return liveInstance == null ? handleNullValueFallback(returnType) : fieldGetter.get(liveInstance);
                                }

                                // 6. Fallback Standard Method Execution Routing
                                Reflector methodInvoker = Reflector.on(targetReflectionClass).method(methodName, parameterTypes);
                                if (isStaticContext) {
                                    return methodInvoker.call(args);
                                }
                                return liveInstance == null ? handleNullValueFallback(returnType) : methodInvoker.callByCaller(liveInstance, args);

                            } catch (Throwable th) {
                                if (IS_DEBUG_MODE) {
                                    Throwable rootCause = th.getCause();
                                    if (rootCause != null) {
                                        rootCause.printStackTrace();
                                    } else {
                                        th.printStackTrace();
                                    }
                                }

                                if (th instanceof BlackNullPointerException) {
                                    throw new NullPointerException(th.getMessage());
                                }
                                if (throwOnFailure) {
                                    throw th;
                                }
                                return handleNullValueFallback(returnType);
                            }
                        }
                    }
            );

            // Populate proxy caches if evaluating static reflection singletons
            if (targetInstance == null) {
                if (throwOnFailure) {
                    STATIC_PROXY_EXCEPTION_CACHE.put(proxyInterface, dynamicProxy);
                } else {
                    STATIC_PROXY_CACHE.put(proxyInterface, dynamicProxy);
                }
            }
            return dynamicProxy;

        } catch (ClassNotFoundException e) {
            if (IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Resolves appropriate structural replacement tokens or primitives matching interface runtime execution bounds.
     */
    @Nullable
    private static Object handleNullValueFallback(@NonNull Class<?> returnType) {
        if (returnType == Void.TYPE) {
            return null; // Proxies intercepting void methods must return null
        }
        if (returnType.isPrimitive()) {
            throw new BlackNullPointerException("Target method execution pipeline evaluated to null returning a required primitive!");
        }
        return null;
    }

    /**
     * Maps proxy metadata descriptions onto targeted system component entities.
     */
    @NonNull
    private static Class<?> resolveTargetReflectionClass(@NonNull Class<?> proxyInterface) throws ClassNotFoundException {
        ZClass directClass = proxyInterface.getAnnotation(ZClass.class);
        ZClassName nameQuery = proxyInterface.getAnnotation(ZClassName.class);
        ZClassNameNotProcess lazyNameQuery = proxyInterface.getAnnotation(ZClassNameNotProcess.class);

        if (directClass == null && nameQuery == null && lazyNameQuery == null) {
            throw new RuntimeException("Structural reflection target missing declaration components! Expected @ZClass, @ZClassName, or @ZClassNameNotProcess.");
        }

        if (directClass != null) {
            return directClass.value();
        }
        return nameQuery != null ? Class.forName(nameQuery.value()) : Class.forName(lazyNameQuery.value());
    }

    /**
     * Parses explicit reflection parameter signatures, evaluating custom type overrides over traditional parameters.
     */
    @NonNull
    private static Class<?>[] resolveParameterTypes(@NonNull Method method) throws Throwable {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class<?>[] standardParameterTypes = method.getParameterTypes();
        Class<?>[] resolvedParameterTypes = new Class[standardParameterTypes.length];

        for (int i = 0; i < standardParameterTypes.length; i++) {
            Annotation[] currentAnnotations = parameterAnnotations[i];
            boolean hasOverridingAnnotation = false;

            for (Annotation annotation : currentAnnotations) {
                if (annotation instanceof ZParamClassName) {
                    resolvedParameterTypes[i] = Class.forName(((ZParamClassName) annotation).value());
                    hasOverridingAnnotation = true;
                    break;
                }
                if (annotation instanceof ZParamClass) {
                    resolvedParameterTypes[i] = ((ZParamClass) annotation).value();
                    hasOverridingAnnotation = true;
                    break;
                }
            }

            if (!hasOverridingAnnotation) {
                resolvedParameterTypes[i] = standardParameterTypes[i];
            }
        }
        return resolvedParameterTypes;
    }

    /**
     * Internal cache accessor matching target configuration scopes.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private static <T> T getCachedProxy(@NonNull Class<T> proxyInterface, @Nullable Object targetInstance, boolean throwOnFailure) {
        if (targetInstance != null) {
            return null; // Instance level proxies avoid default global singleton map lookups
        }
        try {
            return throwOnFailure ? (T) STATIC_PROXY_EXCEPTION_CACHE.get(proxyInterface) : (T) STATIC_PROXY_CACHE.get(proxyInterface);
        } catch (Throwable ignored) {
            return null;
        }
    }
}