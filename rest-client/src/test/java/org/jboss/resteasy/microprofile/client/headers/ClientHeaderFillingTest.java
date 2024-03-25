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

package org.jboss.resteasy.microprofile.client.headers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.annotations.jaxrs.HeaderParam;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ClientHeaderFillingTest {
    private static final String HEADER_NAME = "GENERATED_HEADER";

    private static UndertowJaxrsServer server;
    private static WeldContainer container;

    @BeforeAll
    public static void init() {
        Weld weld = new Weld();
        weld.addBeanClass(HeaderPassingResource.class);
        weld.addBeanClass(HeaderSendingClient.class);
        weld.addBeanClass(ClientInvokingBean.class);
        container = weld.initialize();
        server = new UndertowJaxrsServer().start();
        server.deploy(MyApp.class);
    }

    @Test
    public void checkIfFillerFactoryWithHigherPrioritySelected() {
        List<String> result = container.select(ClientInvokingBean.class).get().getHeaders();
        Assertions.assertTrue(hasItems(result, "high", "prio"));
    }

    @AfterAll
    public static void stop() {
        server.stop();
        container.shutdown();
    }

    @Path("/")
    @RegisterRestClient(baseUri = "http://localhost:8081")
    public interface HeaderSendingClient {
        @GET
        @ClientHeaderParam(name = HEADER_NAME, value = "{someMethod}")
        String headerValues();

        default List<String> someMethod() {
            return Arrays.asList("foo", "bar");
        }
    }

    @Path("/")
    public static class HeaderPassingResource {
        @GET
        public String headerValues(@HeaderParam(HEADER_NAME) List<String> headers) {
            return String.join(",", headers);
        }
    }

    @ApplicationScoped
    public static class ClientInvokingBean {
        @RestClient
        @Inject
        private HeaderSendingClient client;

        public List<String> getHeaders() {
            String headers = client.headerValues();
            return Arrays.asList(headers.split(","));
        }
    }

    @ApplicationPath("")
    public static class MyApp extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<>();
            classes.add(HeaderPassingResource.class);
            return classes;
        }
    }

    private static boolean hasItems(Collection<String> items, String... controlList) {
        return items.containsAll(List.of(controlList));
    }
}
