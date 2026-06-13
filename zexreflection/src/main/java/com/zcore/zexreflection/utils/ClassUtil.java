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
import com.zcore.zexreflection.annotation.ZClass;
import com.zcore.zexreflection.annotation.ZClassName;
import com.zcore.zexreflection.annotation.ZClassNameNotProcess;

/**
 * Utility class for dynamically resolving and loading target classes 
 * backed by metadata annotations.
 */
public final class ClassUtil {

    private ClassUtil() {
        // Utility class containing only static methods
    }

    /**
     * Evaluates a class configuration stub and resolves its real runtime target class 
     * based on structural proxy annotations.
     *
     * @param sourceClass The proxy or configuration class stub containing the reflection annotations.
     * @return The resolved target {@link Class}, or {@code null} if no explicit mapping 
     * could be determined or safely loaded.
     */
    @Nullable
    public static Class<?> resolveTargetClass(@NonNull Class<?> sourceClass) {
        ZClassNameNotProcess skipProcessingAnnotation = sourceClass.getAnnotation(ZClassNameNotProcess.class);
        if (skipProcessingAnnotation != null) {
            return loadClassFromName(skipProcessingAnnotation.value());
        }

        ZClass directClassAnnotation = sourceClass.getAnnotation(ZClass.class);
        if (directClassAnnotation != null) {
            return directClassAnnotation.value();
        }

        ZClassName nameQueryAnnotation = sourceClass.getAnnotation(ZClassName.class);
        if (nameQueryAnnotation != null) {
            return loadClassFromName(nameQueryAnnotation.value());
        }

        return null;
    }

    /**
     * Safely lookup a class type context descriptor matching a string signature.
     * Swallows the generic lookup exception securely to keep operations fluid.
     */
    @Nullable
    private static Class<?> loadClassFromName(@NonNull String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException unused) {
            return null;
        }
    }
}