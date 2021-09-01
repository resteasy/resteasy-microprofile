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

package org.jboss.resteasy.context;

import java.util.Collections;
import java.util.Map;

import org.eclipse.microprofile.context.spi.ThreadContextProvider;
import org.eclipse.microprofile.context.spi.ThreadContextSnapshot;
import org.jboss.resteasy.core.ResteasyContext;

public class ResteasyContextProvider implements ThreadContextProvider {

    private static final String JAXRS_CONTEXT = "JAX-RS";

    @Override
    public ThreadContextSnapshot currentContext(Map<String, String> props) {
        Map<Class<?>, Object> context = ResteasyContext.getContextDataMap();
        return () -> {
            ResteasyContext.pushContextDataMap(context);
            return ResteasyContext::removeContextDataLevel;
        };
    }

    @Override
    public ThreadContextSnapshot clearedContext(Map<String, String> props) {
        Map<Class<?>, Object> context = Collections.emptyMap();
        return () -> {
            ResteasyContext.pushContextDataMap(context);
            return ResteasyContext::removeContextDataLevel;
        };
    }

    @Override
    public String getThreadContextType() {
        return JAXRS_CONTEXT;
    }
}
