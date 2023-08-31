/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.WriterInterceptor;

/**
 * A simple helper for various CDI tasks.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class CdiHelper {

    private static final List<Class<?>> REST_INTERFACES = List.of(
            ContainerRequestFilter.class,
            ContainerResponseFilter.class,
            ContextResolver.class,
            DynamicFeature.class,
            ExceptionMapper.class,
            Feature.class,
            MessageBodyReader.class,
            MessageBodyWriter.class,
            ParamConverterProvider.class,
            ReaderInterceptor.class,
            WriterInterceptor.class);

    /**
     * Finds out if a given class is decorated with JAX-RS annotations.
     * Interfaces of the class are not scanned for JAX-RS annotations.
     *
     * @param clazz class
     *
     * @return true if a given interface has @Path annotation or if any of its
     *         methods is decorated with @Path annotation or a request method
     *         designator.
     */
    static boolean isRestAnnotatedClass(final Class<?> clazz) {
        if (clazz.isAnnotationPresent(Path.class)) {
            return true;
        }
        if (clazz.isAnnotationPresent(Provider.class)) {
            return true;
        }
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Path.class)) {
                return true;
            }
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType().isAnnotationPresent(HttpMethod.class)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if and only if the given class is a JAX-RS root resource or a
     * sub-resource. The class itself as well as its interfaces are scanned for
     * JAX-RS annotations.
     *
     * @param clazz class
     *
     * @return true if the given class is JAX-RS resource or sub-resource
     */
    static boolean isRestResource(final Class<?> clazz) {
        if (isRestAnnotatedClass(clazz)) {
            return true;
        }
        // Check if this implements any known Jakarta REST interfaces
        for (Class<?> intf : REST_INTERFACES) {
            if (intf.isAssignableFrom(clazz)) {
                return true;
            }
        }
        for (Class<?> intf : clazz.getInterfaces()) {
            if (isRestAnnotatedClass(intf)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find out if a given class is a JAX-RS component
     *
     * @param clazz class
     *
     * @return true if and only if a give class is a JAX-RS resource, provider or
     *         jakarta.ws.rs.core.Application subclass.
     */
    static boolean isRestComponent(final Class<?> clazz) {
        return ((clazz.isAnnotationPresent(Provider.class)) || (isRestResource(clazz))
                || (Application.class.isAssignableFrom(clazz)));
    }
}
