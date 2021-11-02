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

package org.jboss.resteasy.microprofile.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ClientHeadersDefaultFactoryCDITest {

    private UndertowJaxrsServer undertowJaxrsServer;
    private WeldContainer weldContainer;

    static class Worker {

        @Inject
        @RestClient
        private ClientHeaderParamIntf service;

        public String work() {
            return service.hello("Stefano");
        }
    }

    @Path("/")
    @RegisterRestClient(baseUri = "http://localhost:8081")
    @RegisterClientHeaders
    @ClientHeaderParam(name = "IntfHeader", value = "intfValue")
    public interface ClientHeaderParamIntf {

        @Path("hello/{h}")
        @GET
        @ClientHeaderParam(name = "MthdHeader", value = "hello")
        String hello(@PathParam("h") String h);
    }

    @Path("/")
    public static class ClientParamResource {

        @Path("hello/{h}")
        @GET
        public String hello(@PathParam("h") String h, @Context HttpHeaders httpHeaders) {
            return getValue(httpHeaders, "IntfHeader")
                    + " - "
                    + getValue(httpHeaders, "MthdHeader");
        }

        private String getValue(HttpHeaders httpHeaders, String headerName) {
            List<String> values = httpHeaders.getRequestHeader(headerName);
            if (values.size() > 0) {
                return headerName + ": " + values.get(0);
            }

            return headerName + ": NO VALUE";
        }
    }

    @ApplicationPath("")
    public static class MyApp extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<>();
            classes.add(ClientParamResource.class);
            return classes;
        }
    }

    @Before
    public void init() {
        Weld weld = new Weld();
        weld.addBeanClass(Worker.class);
        weld.addBeanClass(ClientHeaderParamIntf.class);
        weldContainer = weld.initialize();
        undertowJaxrsServer = new UndertowJaxrsServer().start();
        undertowJaxrsServer.deploy(MyApp.class);
    }

    @After
    public void stop() {
        if (undertowJaxrsServer != null) {
            undertowJaxrsServer.stop();
        }
        if (weldContainer != null) {
            weldContainer.close();
        }
    }

    @Test
    public void test() {
        String result = weldContainer.select(Worker.class).get().work();
        Assert.assertTrue(result.contains("IntfHeader: intfValue"));
        Assert.assertTrue(result.contains("MthdHeader: hello"));
    }
}
