/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2021 Red Hat, Inc., and individual contributors
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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.spi.RestClientListener;

public class RestClientListeners {

    private RestClientListeners() {
    }

    /**
     * A synchronized weak hash map that keeps RestClientListener instances retrieved using ServiceLoader for each classloader.
     * Weak keys are used to remove entries when classloaders are garbage collected.
     */
    private static final Map<ClassLoader, Collection<RestClientListener>> map = Collections
            .synchronizedMap(new WeakHashMap<>());

    public static Collection<RestClientListener> get() {
        ClassLoader loader = SecurityActions.getClassLoader(RestClientListeners.class);
        if (loader == null) {
            return Collections.emptyList();
        }
        final PrivilegedAction<Collection<RestClientListener>> action = () -> ServiceLoader
                .load(RestClientListener.class, loader)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toUnmodifiableList());
        return map.computeIfAbsent(loader, classLoader -> {
            if (System.getSecurityManager() == null) {
                return action.run();
            }
            return AccessController.doPrivileged(action);
        });
    }
}
