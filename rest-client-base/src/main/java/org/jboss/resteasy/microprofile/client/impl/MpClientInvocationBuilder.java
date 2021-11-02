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

package org.jboss.resteasy.microprofile.client.impl;

import java.net.URI;

import javax.ws.rs.client.CompletionStageRxInvoker;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ClientConfiguration;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocationBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientRequestHeaders;
import org.jboss.resteasy.microprofile.client.async.AsyncInterceptorRxInvoker;

public class MpClientInvocationBuilder extends ClientInvocationBuilder {

    public MpClientInvocationBuilder(final ResteasyClient client, final URI uri,
            final ClientConfiguration configuration) {
        super(client, uri, configuration);
    }

    @Override
    public CompletionStageRxInvoker rx() {
        return new AsyncInterceptorRxInvoker(this, invocation.getClient().asyncInvocationExecutor());
    }

    @Override
    protected ClientInvocation createClientInvocation(ClientInvocation invocation) {
        return new MpClientInvocation(invocation);
    }

    @Override
    protected ClientInvocation createClientInvocation(ResteasyClient client, URI uri, ClientRequestHeaders headers,
            ClientConfiguration parent) {
        return new MpClientInvocation(client, uri, headers, parent);
    }
}
