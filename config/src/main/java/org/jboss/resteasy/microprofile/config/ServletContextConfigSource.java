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

package org.jboss.resteasy.microprofile.config;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;

import org.jboss.resteasy.core.ResteasyContext;

public class ServletContextConfigSource implements ResteasyConfigSource {

    private volatile String name;

    @Override
    public Map<String, String> getProperties() {
        ServletContext context = ResteasyContext.getContextData(ServletContext.class);
        if (context == null) {
            return Collections.emptyMap();
        }
        final Map<String, String> map = new LinkedHashMap<>();
        final Enumeration<String> keys = context.getInitParameterNames();
        if (keys != null) {
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                map.put(key, context.getInitParameter(key));
            }
        }
        return map;
    }

    @Override
    public Set<String> getPropertyNames() {
        final ServletContext context = ResteasyContext.getContextData(ServletContext.class);
        if (context == null) {
            return Collections.emptySet();
        }

        return new LinkedHashSet<>(Collections.list(context.getInitParameterNames()));
    }

    @Override
    public String getValue(String propertyName) {
        final ServletContext context = ResteasyContext.getContextData(ServletContext.class);
        if (context == null) {
            return null;
        }
        return context.getInitParameter(propertyName);
    }

    @Override
    public String getName() {
        String currentName = name;
        if (currentName == null) {
            synchronized (this) {
                currentName = name;
                if (currentName == null) {
                    final ServletContext servletContext = ResteasyContext.getContextData(ServletContext.class);
                    final StringBuilder sb = new StringBuilder();
                    currentName = sb.append(servletContext != null ? servletContext.getServletContextName() : null)
                            .append(":ServletContextConfigSource").toString();
                    this.name = currentName;
                }
            }
        }
        return currentName;
    }

    @Override
    public int getDefaultOrdinal() {
        return 40;
    }

}
