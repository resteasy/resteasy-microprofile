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

package org.jboss.resteasy.microprofile.client.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptorFactory;
import org.jboss.resteasy.spi.PriorityComparator;
import org.jboss.resteasy.spi.concurrent.ThreadContext;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class AsyncInvocationInterceptorThreadContext implements ThreadContext<List<AsyncInvocationInterceptor>> {
    private final List<AsyncInvocationInterceptorFactory> factories;

    public AsyncInvocationInterceptorThreadContext(final Collection<AsyncInvocationInterceptorFactory> factories) {
        this.factories = new ArrayList<>(factories);
        this.factories.sort(new PriorityComparator<>());
    }

    @Override
    public List<AsyncInvocationInterceptor> capture() {
        final List<AsyncInvocationInterceptor> captured = new ArrayList<>();
        for (AsyncInvocationInterceptorFactory factory : factories) {
            final AsyncInvocationInterceptor interceptor = factory.newInterceptor();
            interceptor.prepareContext();
            captured.add(interceptor);
        }
        return captured;
    }

    @Override
    public void push(final List<AsyncInvocationInterceptor> context) {
        if (context != null) {
            context.forEach(AsyncInvocationInterceptor::applyContext);
        }
    }

    @Override
    public void reset(final List<AsyncInvocationInterceptor> context) {
        if (context != null) {
            context.forEach(AsyncInvocationInterceptor::removeContext);
        }
    }
}
