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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ResponseProcessingException;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;

import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.microprofile.client.header.ClientHeaderFillingException;

public class ProxyInvocationHandler implements InvocationHandler {

    private static final Logger LOGGER = Logger.getLogger(ProxyInvocationHandler.class);
    public static final Type[] NO_TYPES = {};

    private final Object target;

    private final Set<Object> providerInstances;

    private final ResteasyClient client;
    private final ClassLoader classLoader;

    private final AtomicBoolean closed;

    public ProxyInvocationHandler(final Class<?> restClientInterface,
                                  final Object target,
                                  final Set<Object> providerInstances,
                                  final ResteasyClient client,
                                  final ClassLoader classLoader) {
        this.target = target;
        this.providerInstances = providerInstances;
        this.client = client;
        this.classLoader = classLoader;
        this.closed = new AtomicBoolean();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (RestClientProxy.class.equals(method.getDeclaringClass())) {
            return invokeRestClientProxyMethod(proxy, method, args);
        }
        // Autocloseable/Closeable
        if (method.getName().equals("close") && (args == null || args.length == 0)) {
            close();
            return null;
        }
        if (closed.get()) {
            throw new IllegalStateException("RestClientProxy is closed");
        }

        boolean replacementNeeded = false;
        Object[] argsReplacement = args != null ? new Object[args.length] : null;
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        if (args != null) {
            for (Object p : providerInstances) {
                if (p instanceof ParamConverterProvider) {

                    int index = 0;
                    for (Object arg : args) {
                        // ParamConverter's are not allowed to be passed null values. If we have a null value do not process
                        // it through the provider.
                        if (arg == null) {
                            continue;
                        }

                        if (parameterAnnotations[index].length > 0) { // does a parameter converter apply?
                            ParamConverter<?> converter = ((ParamConverterProvider) p).getConverter(arg.getClass(), null,
                                    parameterAnnotations[index]);
                            if (converter != null) {
                                Type[] genericTypes = getGenericTypes(converter.getClass());
                                if (genericTypes.length == 1) {

                                    // minimum supported types
                                    switch (genericTypes[0].getTypeName()) {
                                        case "java.lang.String":
                                            @SuppressWarnings("unchecked")
                                            ParamConverter<String> stringConverter = (ParamConverter<String>) converter;
                                            argsReplacement[index] = stringConverter.toString((String) arg);
                                            replacementNeeded = true;
                                            break;
                                        case "java.lang.Integer":
                                            @SuppressWarnings("unchecked")
                                            ParamConverter<Integer> intConverter = (ParamConverter<Integer>) converter;
                                            argsReplacement[index] = intConverter.toString((Integer) arg);
                                            replacementNeeded = true;
                                            break;
                                        case "java.lang.Boolean":
                                            @SuppressWarnings("unchecked")
                                            ParamConverter<Boolean> boolConverter = (ParamConverter<Boolean>) converter;
                                            argsReplacement[index] = boolConverter.toString((Boolean) arg);
                                            replacementNeeded = true;
                                            break;
                                        default:
                                            continue;
                                    }
                                }
                            }
                        } else {
                            argsReplacement[index] = arg;
                        }
                        index++;
                    }
                }
            }
        }

        if (replacementNeeded) {
            args = argsReplacement;
        }

        try {
            Object returnValue = method.invoke(target, args);

            // SubResources interfaces must be proxified as well
            // Those methods lack a jax-rs http method annotation, but have a Path annotation and return another interface.
            Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
            Class<?> returnType = method.getReturnType();
            boolean hasHttpMethodAnnotation = Arrays.stream(declaredAnnotations)
                    .anyMatch(a -> a.annotationType().getDeclaredAnnotation(HttpMethod.class) != null);
            boolean hasPathAnnotation = Arrays.stream(declaredAnnotations)
                    .anyMatch(a -> a.annotationType().equals(Path.class));
            boolean isInterfaceReturned = returnType.isInterface();
            boolean isSubresourceMethod = !hasHttpMethodAnnotation && hasPathAnnotation && isInterfaceReturned;
            if (isSubresourceMethod) {
                // Instantiate a new proxy for the subresource
                ProxyInvocationHandler subresourceProxyInvocationhandler = new ProxyInvocationHandler(returnType, returnValue,
                        providerInstances, client, classLoader);
                return Proxy.newProxyInstance(classLoader, new Class[]{returnType}, subresourceProxyInvocationhandler);
            } else {
                return returnValue;
            }
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CompletionException) {
                cause = cause.getCause();
            }
            if (cause instanceof ExceptionMapping.HandlerException) {
                ((ExceptionMapping.HandlerException) cause).mapException(method);
                // no applicable exception mapper found or applicable mapper returned null
                return null;
            }
            if (cause instanceof ResponseProcessingException) {
                ResponseProcessingException rpe = (ResponseProcessingException) cause;
                cause = rpe.getCause();
                if (cause instanceof RuntimeException) {
                    throw cause;
                }
            } else {
                if (cause instanceof ProcessingException &&
                        cause.getCause() instanceof ClientHeaderFillingException) {
                    throw cause.getCause().getCause();
                }
                if (cause instanceof RuntimeException) {
                    throw cause;
                }
            }
            throw e;
        }
    }

    private Object invokeRestClientProxyMethod(Object proxy, Method method, Object[] args) {
        switch (method.getName()) {
            case "getClient":
                return client;
            case "close":
                close();
                return null;
            default:
                throw new IllegalStateException("Unsupported RestClientProxy method: " + method);
        }
    }

    private void close() {
        if (closed.compareAndSet(false, true)) {
            client.close();
        }
    }

    private Type[] getGenericTypes(Class<?> aClass) {
        Type[] genericInterfaces = aClass.getGenericInterfaces();
        Type[] genericTypes = NO_TYPES;
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                genericTypes = ((ParameterizedType) genericInterface).getActualTypeArguments();
            }
        }
        return genericTypes;
    }
}
