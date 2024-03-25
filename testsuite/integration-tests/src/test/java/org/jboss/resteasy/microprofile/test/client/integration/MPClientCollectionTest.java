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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MPCollectionActivator;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MPCollectionResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MPCollectionService;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MPCollectionServiceIntf;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @tpSubChapter MicroProfile Config
 * @tpChapter Integration tests
 * @tpTestCaseDetails Show how to use injection to get access to the service.
 *                    Show configuration required for a GenericType return type.
 * @tpSince RESTEasy 4.6.0
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class MPClientCollectionTest {
    protected static final Logger LOG = Logger.getLogger(MPCollectionTest.class.getName());
    private static final String WAR_SERVICE = "war_service";
    private static final String WAR_CLIENT = "war_client";

    @Deployment(name = WAR_SERVICE)
    public static Archive<?> serviceDeploy() {
        return TestEnvironment.createWar(WAR_SERVICE)
                .addClasses(MPCollectionService.class, MPCollectionActivator.class);
    }

    @Deployment(name = WAR_CLIENT)
    public static Archive<?> clientDeploy() throws IOException {
        return TestEnvironment.addConfigProperties(TestEnvironment.createWar(WAR_CLIENT)
                .addClasses(MPCollectionActivator.class, MPCollectionResource.class,
                        MPCollectionServiceIntf.class),
                Collections.singletonMap(MPCollectionServiceIntf.class.getCanonicalName() + "/mp-rest/url", TestEnvironment
                        .getHttpUrl() + WAR_SERVICE));
    }

    static Client client;

    @ArquillianResource
    @OperateOnDeployment(WAR_SERVICE)
    private URL warUrl;

    @ArquillianResource
    @OperateOnDeployment(WAR_CLIENT)
    private URL clientUrl;

    @BeforeAll
    public static void before() throws Exception {
        client = ClientBuilder.newClient();
    }

    @AfterAll
    public static void after() throws Exception {
        client.close();
    }

    @Test
    public void preTest() throws Exception {
        // pre-test, confirm the service is reachable
        // If this test fails the other tests will not pass
        Response response = client.target(
                TestEnvironment.generateUri(warUrl, "/theService/ping")).request().get();
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals("pong", response.readEntity(String.class));
    }

    @Test
    public void testStringReturnType() throws Exception {

        // Test service is accessed via injection
        // Test endpoint with simple (String) return type
        Response response = client.target(
                TestEnvironment.generateUri(clientUrl, "/theService/thePatron/checking")).request().get();
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals("pong thePatron", response.readEntity(String.class));
    }

    @Test
    public void testGenericTypeReturnType() throws Exception {
        // Test service is accessed via injection
        // Test endpoint with GenericType return type
        Response response = client.target(
                TestEnvironment.generateUri(clientUrl, "/theService/thePatron/got")).request().get();
        Assertions.assertEquals(200, response.getStatus());
        List<String> l = response.readEntity(new GenericType<List<String>>() {
        });
        Assertions.assertEquals(4, l.size());
        Assertions.assertEquals("thePatron", l.get(3));
    }
}
