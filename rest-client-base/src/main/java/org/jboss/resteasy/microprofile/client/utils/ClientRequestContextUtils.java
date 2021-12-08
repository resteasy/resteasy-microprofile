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

package org.jboss.resteasy.microprofile.client.utils;

import java.lang.reflect.Method;

import jakarta.ws.rs.client.ClientRequestContext;

import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientRequestContextImpl;

/**
 * A utility class to pull out common operations on {@link ClientRequestContext}
 */
public class ClientRequestContextUtils {

    /**
     * Get {@link Method} for the client call from {@link ClientRequestContext}
     *
     * @param requestContext the context
     *
     * @return the method
     */
    public static Method getMethod(ClientRequestContext requestContext) {
        if (requestContext instanceof ClientRequestContextImpl == false) {
            throw new RuntimeException(
                    "Failed to get ClientInvocation from request context. Is RestEasy client used underneath?");
        }
        ClientInvocation invocation = ((ClientRequestContextImpl) requestContext).getInvocation();
        return invocation.getClientInvoker().getMethod();
    }

    /**
     * Get {@link Class} for the client call from {@link ClientRequestContext}
     *
     * @param requestContext the context
     *
     * @return the class
     */
    public static Class<?> getDeclaringClass(ClientRequestContext requestContext) {
        if (requestContext instanceof ClientRequestContextImpl == false) {
            throw new RuntimeException(
                    "Failed to get ClientInvocation from request context. Is RestEasy client used underneath?");
        }
        ClientInvocation invocation = ((ClientRequestContextImpl) requestContext).getInvocation();
        return invocation.getClientInvoker().getDeclaring();
    }

    private ClientRequestContextUtils() {
    }
}
