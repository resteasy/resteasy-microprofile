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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MPCollectionActivator;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MPCollectionResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MPCollectionService;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MPCollectionServiceIntf;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @tpSubChapter MicroProfile Config
 * @tpChapter Integration tests
 * @tpTestCaseDetails Show how to use injection to get access to the service.
 *                    Show configuration required for a GenericType return type.
 * @tpSince RESTEasy 4.6.0
 */
@RunWith(Arquillian.class)
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

    @BeforeClass
    public static void before() throws Exception {
        client = ClientBuilder.newClient();
    }

    @AfterClass
    public static void after() throws Exception {
        client.close();
    }

    @Test
    public void preTest() throws Exception {
        // pre-test, confirm the service is reachable
        // If this test fails the other tests will not pass
        Response response = client.target(
                TestEnvironment.generateUri(warUrl, "/theService/ping")).request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("pong", response.readEntity(String.class));
    }

    @Test
    public void testStringReturnType() throws Exception {

        // Test service is accessed via injection
        // Test endpoint with simple (String) return type
        Response response = client.target(
                TestEnvironment.generateUri(clientUrl, "/theService/thePatron/checking")).request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("pong thePatron", response.readEntity(String.class));
    }

    @Test
    public void testGenericTypeReturnType() throws Exception {
        // Test service is accessed via injection
        // Test endpoint with GenericType return type
        Response response = client.target(
                TestEnvironment.generateUri(clientUrl, "/theService/thePatron/got")).request().get();
        Assert.assertEquals(200, response.getStatus());
        List<String> l = response.readEntity(new GenericType<List<String>>() {
        });
        Assert.assertEquals(4, l.size());
        Assert.assertEquals("thePatron", l.get(3));
    }
}
