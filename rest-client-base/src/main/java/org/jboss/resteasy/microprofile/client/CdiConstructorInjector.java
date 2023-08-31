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

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.ws.rs.WebApplicationException;

import org.jboss.resteasy.spi.ApplicationException;
import org.jboss.resteasy.spi.ConstructorInjector;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;

/**
 * A {@linkplain ConstructorInjector constructor injector} which uses CDI to lookup beans for injected values. If no
 * CDI bean was found, standard Jakarta REST injection is used.
 *
 * @author Jozef Hartinger
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class CdiConstructorInjector implements ConstructorInjector {
    private final BeanManager manager;
    private final Type type;

    public CdiConstructorInjector(final Type type, final BeanManager manager) {
        this.type = type;
        this.manager = manager;
    }

    @Override
    public Object construct(boolean unwrapAsync) {
        Set<Bean<?>> beans = manager.getBeans(type);

        if (beans.size() > 1) {
            Set<Bean<?>> modifiableBeans = new HashSet<>(beans);
            // Ambiguous dependency may occur if a resource has subclasses
            // Therefore we remove those beans
            // remove Beans that have clazz in their type closure but not as a base class
            modifiableBeans.removeIf(bean -> !bean.getBeanClass().equals(type) && !bean.isAlternative());
            beans = modifiableBeans;
        }

        Bean<?> bean = manager.resolve(beans);
        CreationalContext<?> context = manager.createCreationalContext(bean);
        return manager.getReference(bean, type, context);
    }

    @Override
    public Object construct(HttpRequest request, HttpResponse response, boolean unwrapAsync)
            throws Failure, WebApplicationException, ApplicationException {
        return construct(unwrapAsync);
    }

    @Override
    public Object injectableArguments(boolean unwrapAsync) {
        return null;
    }

    @Override
    public Object injectableArguments(HttpRequest request, HttpResponse response, boolean unwrapAsync) throws Failure {
        return injectableArguments(unwrapAsync);
    }
}
