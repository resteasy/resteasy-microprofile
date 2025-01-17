/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2025 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.resteasy.microprofile.client;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * This class <strong>must</strong> never be made public.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class SecurityActions {

    /**
     * Returns the current threads context class loader.
     *
     * @return the current threads context class loader or {@code null} if there is not one
     */
    static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> Thread.currentThread()
                .getContextClassLoader());
    }

    /**
     * Gets the current context class loader or the class loader of the passed in class.
     *
     * @param clazz the class to get the class loader from if the context class loader is {@code null}
     *
     * @return the available class loader
     */
    static ClassLoader getClassLoader(final Class<?> clazz) {
        if (System.getSecurityManager() == null) {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            return cl == null ? clazz.getClassLoader() : cl;
        }
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            return cl == null ? clazz.getClassLoader() : cl;
        });
    }
}
