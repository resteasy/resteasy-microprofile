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
import javax.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.rest.client.ext.QueryParamStyle;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ClientConfiguration;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocationBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientWebTarget;

public class MpClientWebTarget extends ClientWebTarget {

    private final QueryParamStyle queryParamStyle;

    protected MpClientWebTarget(final ResteasyClient client, final ClientConfiguration configuration,
                                final QueryParamStyle queryParamStyle) {
        super(client, configuration);
        this.queryParamStyle = queryParamStyle;
    }

    public MpClientWebTarget(final ResteasyClient client, final String uri, final ClientConfiguration configuration,
                             final QueryParamStyle queryParamStyle)
            throws IllegalArgumentException, NullPointerException {
        super(client, new MpUriBuilder().uri(uri, queryParamStyle), configuration);
        this.queryParamStyle = queryParamStyle;
    }

    public MpClientWebTarget(final ResteasyClient client, final URI uri, final ClientConfiguration configuration,
                             final QueryParamStyle queryParamStyle) throws NullPointerException {
        super(client, new MpUriBuilder().uri(uri, queryParamStyle), configuration);
        this.queryParamStyle = queryParamStyle;
    }

    public MpClientWebTarget(final ResteasyClient client, final UriBuilder uriBuilder,
                             final ClientConfiguration configuration,
                             final QueryParamStyle queryParamStyle) throws NullPointerException {
        super(client, uriBuilder, configuration);
        this.queryParamStyle = queryParamStyle;
    }

    @Override
    protected ClientWebTarget newInstance(ResteasyClient client, UriBuilder uriBuilder,
                                          ClientConfiguration configuration) {
        return new MpClientWebTarget(client, uriBuilder, configuration, queryParamStyle);
    }

    @Override
    protected ClientInvocationBuilder createClientInvocationBuilder(ResteasyClient client, URI uri,
                                                                    ClientConfiguration configuration) {
        return new MpClientInvocationBuilder(client, uri, configuration);
    }

}
