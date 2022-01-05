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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.RestClientProxyRedeployRemoteService;
import org.jboss.resteasy.microprofile.test.client.integration.resource.RestClientProxyRedeployResource;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public class RestClientProxyRedeployTest {
    @Deployment(name = "deployment1", order = 1)
    public static Archive<?> deploy1() throws IOException {
        return TestEnvironment.createWar(RestClientProxyRedeployTest.class, "1")
                .addClasses(RestClientProxyRedeployResource.class, RestClientProxyRedeployRemoteService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Deployment(name = "deployment2", order = 2)
    public static Archive<?> deploy2() throws IOException {
        return TestEnvironment.createWar(RestClientProxyRedeployTest.class, "2")
                .addClasses(RestClientProxyRedeployResource.class, RestClientProxyRedeployRemoteService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @ArquillianResource
    @OperateOnDeployment("deployment1")
    private URL url1;

    @ArquillianResource
    @OperateOnDeployment("deployment2")
    private URL url2;

    @Test
    public void testGet1() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(TestEnvironment.generateUri(url1, "/test-app/test/1")).request().get();
        Assert.assertEquals(200, response.getStatus());
        String entity = response.readEntity(String.class);
        Assert.assertEquals("OK", entity);
        client.close();
    }

    @Test
    public void testGet2() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(TestEnvironment.generateUri(url2, "/test-app/test/1")).request().get();
        Assert.assertEquals(200, response.getStatus());
        String entity = response.readEntity(String.class);
        Assert.assertEquals("OK", entity);
        client.close();
    }
}
