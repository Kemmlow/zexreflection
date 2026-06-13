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

import androidx.annotation.Nullable;

/**
 * A specialized {@link NullPointerException} thrown by the reflection engine 
 * when a critical structural target, internal configuration map, or proxy wrapper 
 * resolves unexpectedly to {@code null}.
 * <p>
 * This distinct exception variant allows consumer frameworks to segregate standard 
 * application null-pointer anomalies from internal reflection orchestration layer failures.
 */
public class BlackNullPointerException extends NullPointerException {
    
    private static final long serialVersionUID = 42L; // Standard practice for Serializable structures

    /**
     * Constructs a new {@code BlackNullPointerException} with no detailed diagnostic message.
     */
    public BlackNullPointerException() {
        super();
    }

    /**
     * Constructs a new {@code BlackNullPointerException} with a specified contextual message.
     *
     * @param message The diagnostic message saved for later retrieval by the {@link #getMessage()} method.
     */
    public BlackNullPointerException(@Nullable String message) {
        super(message);
    }
}