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

package org.jboss.resteasy.microprofile.test.client.integration.resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptorFactory;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class TestAsyncInvocationInterceptorFactory implements AsyncInvocationInterceptorFactory {

    public static final ThreadLocal<Map<String, String>> localState = ThreadLocal.withInitial(ConcurrentHashMap::new);

    private final CountDownLatch removed;
    private volatile Map<String, String> currentContext;

    public TestAsyncInvocationInterceptorFactory(final CountDownLatch removed) {
        this.removed = removed;
    }

    @Override
    public AsyncInvocationInterceptor newInterceptor() {
        return new AsyncInvocationInterceptor() {
            @Override
            public void prepareContext() {
                currentContext = localState.get();
                localState.get().put("prepareContext", Thread.currentThread().getName());
            }

            @Override
            public void applyContext() {
                localState.set(currentContext);
                currentContext.put("applyContext", Thread.currentThread().getName());
            }

            @Override
            public void removeContext() {
                currentContext.put("removeContext", Thread.currentThread().getName());
                localState.remove();
                currentContext = null;
                removed.countDown();
            }
        };
    }
}
