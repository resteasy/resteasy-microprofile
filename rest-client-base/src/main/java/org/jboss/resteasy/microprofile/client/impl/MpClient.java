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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import jakarta.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.rest.client.ext.QueryParamStyle;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.ClientConfiguration;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientImpl;

public class MpClient extends ResteasyClientImpl {

    private QueryParamStyle queryParamStyle = null;

    public MpClient(final ClientHttpEngine engine, final ExecutorService executor, final boolean cleanupExecutor,
            final ScheduledExecutorService scheduledExecutorService, final ClientConfiguration config) {
        super(engine, executor, cleanupExecutor, scheduledExecutorService, config);
    }

    protected ResteasyWebTarget createClientWebTarget(ResteasyClientImpl client, String uri,
            ClientConfiguration configuration) {
        return new MpClientWebTarget(client, uri, configuration, queryParamStyle);
    }

    protected ResteasyWebTarget createClientWebTarget(ResteasyClientImpl client, URI uri,
            ClientConfiguration configuration) {
        return new MpClientWebTarget(client, uri, configuration, queryParamStyle);
    }

    protected ResteasyWebTarget createClientWebTarget(ResteasyClientImpl client, UriBuilder uriBuilder,
            ClientConfiguration configuration) {
        return new MpClientWebTarget(client, uriBuilder, configuration, queryParamStyle);
    }

    public void setQueryParamStyle(QueryParamStyle queryParamStyle) {
        this.queryParamStyle = queryParamStyle;
    }
}
