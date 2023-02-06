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

import java.io.Closeable;
import java.net.URI;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class CloseableClientTest {

    /**
     * Validates that a client that extends {@link AutoCloseable} works correctly.
     */
    @Test
    public void buildAutoCloseableClientWithUriTemplate() {
        AutoCloseableClientWithUriTemplate client = RestClientBuilder.newBuilder().baseUri(URI.create("http://localhost"))
                .build(AutoCloseableClientWithUriTemplate.class);
        Assert.assertNotNull(client);
    }

    /**
     * Validates that a client that extends {@link Closeable} works correctly.
     */
    @Test
    public void buildCloseableClientWithUriTemplate() {
        CloseableClientWithUriTemplate client = RestClientBuilder.newBuilder().baseUri(URI.create("http://localhost"))
                .build(CloseableClientWithUriTemplate.class);
        Assert.assertNotNull(client);
    }

    @Path("/{name}")
    interface AutoCloseableClientWithUriTemplate extends AutoCloseable {

        @GET
        String hello(@PathParam("name") String name);

    }

    @Path("/{name}")
    interface CloseableClientWithUriTemplate extends Closeable {

        @GET
        String hello(@PathParam("name") String name);

    }
}
