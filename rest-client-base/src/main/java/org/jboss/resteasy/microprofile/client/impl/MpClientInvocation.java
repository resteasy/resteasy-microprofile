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
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ClientConfiguration;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientRequestContextImpl;
import org.jboss.resteasy.client.jaxrs.internal.ClientRequestHeaders;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.core.ResteasyContext;


public class MpClientInvocation extends ClientInvocation {

    public static final String CONTAINER_HEADERS = "MP_CLIENT_CONTAINER_HEADERS";

    private MultivaluedMap<String, String> containerHeaders;
    private List<AsyncInvocationInterceptor> asyncInvocationInterceptors;

    protected MpClientInvocation(final ClientInvocation clientInvocation) {
        super(clientInvocation);
        captureContainerHeaders();
    }

    protected MpClientInvocation(final ResteasyClient client, final URI uri, final ClientRequestHeaders headers,
                                 final ClientConfiguration parent) {
        super(client, uri, headers, parent);
        captureContainerHeaders();
    }

    private void captureContainerHeaders() {
        HttpHeaders containerHeaders = ResteasyContext.getContextData(HttpHeaders.class);
        if (containerHeaders != null) {
            this.containerHeaders = containerHeaders.getRequestHeaders();
        }
    }

    @Override
    protected ClientResponse filterRequest(ClientRequestContextImpl requestContext) {
        if (containerHeaders != null) {
            requestContext.setProperty(CONTAINER_HEADERS, containerHeaders);
        }
        return super.filterRequest(requestContext);
    }
}
