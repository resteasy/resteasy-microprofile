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

package org.jboss.resteasy.microprofile.test.config;

import java.net.URL;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.config.resource.MicroProfileConfigUseGlobalApplication1;
import org.jboss.resteasy.microprofile.test.config.resource.MicroProfileConfigUseGlobalApplication2;
import org.jboss.resteasy.microprofile.test.config.resource.MicroProfileConfigUseGlobalResource;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @tpSubChapter MicroProfile Config: ServletConfig with useGlobal and multiple servlets
 * @tpChapter Integration tests
 * @tpTestCaseDetails Regression tests for RESTEASY-2266
 * @tpSince RESTEasy 4.1.0
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class MicroProfileConfigUseGlobalTest {

    static Client client;
    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> deploy() {
        return TestEnvironment.createWar(MicroProfileConfigUseGlobalTest.class)
                .addClass(MicroProfileConfigUseGlobalApplication1.class)
                .addClass(MicroProfileConfigUseGlobalApplication2.class)
                .addClass(MicroProfileConfigUseGlobalResource.class)
                .setWebXML(MicroProfileConfigUseGlobalTest.class.getPackage(), "web_use_global.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @BeforeAll
    public static void before() throws Exception {
        client = ClientBuilder.newClient();
    }

    @AfterAll
    public static void after() throws Exception {
        client.close();
    }

    /**
     * @tpTestDetails
     * @tpSince RESTEasy 4.1.0
     */
    @Test
    public void testMultipleAppsUseGlobal() throws Exception {
        Response response = client.target(TestEnvironment.generateUri(url, "/app1/prefix")).request().get();
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals("/app1", response.readEntity(String.class));
        response = client.target(TestEnvironment.generateUri(url, "/app2/prefix")).request().get();
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals("/app2", response.readEntity(String.class));
    }
}
