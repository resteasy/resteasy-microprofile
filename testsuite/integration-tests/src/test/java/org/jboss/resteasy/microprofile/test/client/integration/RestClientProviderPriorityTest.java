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

package org.jboss.resteasy.microprofile.test.client.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URL;

import jakarta.annotation.Priority;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class RestClientProviderPriorityTest {
    @ArquillianResource
    URL url;

    @Deployment
    public static Archive<?> deploy() {
        return TestEnvironment.createWar(RestClientProviderPriorityTest.class)
                .addClasses(HelloResource.class, HelloClient.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void helloNaruto() throws Exception {
        HelloClient helloClient = RestClientBuilder.newBuilder()
                .baseUri(TestEnvironment.generateUri(url, "test-app"))
                .build(HelloClient.class);

        assertEquals("Hello Naruto", helloClient.hello("Naruto"));
    }

    @Test
    public void helloBar() throws Exception {
        HelloClient helloClient = RestClientBuilder.newBuilder()
                .baseUri(TestEnvironment.generateUri(url, "test-app"))
                .build(HelloClient.class);

        assertEquals("Hello Bar", helloClient.hello(null));
    }

    @Path("/")
    @Produces("text/plain")
    // Bar should execute first due to lower priority 1 vs Integer.MAX
    @RegisterProviders({ @RegisterProvider(HelloFooProvider.class),
            @RegisterProvider(value = HelloBarProvider.class, priority = 1) })
    public interface HelloClient {
        @GET
        @Path("/hello")
        String hello(@QueryParam("who") String who);
    }

    @Path("/")
    public static class HelloResource {
        @GET
        @Path("/hello")
        public String hello(@QueryParam("who") String who) {
            return "Hello " + who;
        }
    }

    // RESTEASY-2678 - the @Priority annotation was ignored, so the priority would be -1 and this would execute first.
    @Priority(value = Integer.MAX_VALUE)
    public static class HelloFooProvider implements ClientRequestFilter {
        @Override
        public void filter(final ClientRequestContext requestContext) throws IOException {
            if (requestContext.getUri().getQuery() == null) {
                requestContext.setUri(UriBuilder.fromUri(requestContext.getUri()).queryParam("who", "Foo").build());
            }
        }
    }

    public static class HelloBarProvider implements ClientRequestFilter {
        @Override
        public void filter(final ClientRequestContext requestContext) throws IOException {
            if (requestContext.getUri().getQuery() == null) {
                requestContext.setUri(UriBuilder.fromUri(requestContext.getUri()).queryParam("who", "Bar").build());
            }
        }
    }
}
