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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.PropertyPermission;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.config.ServletConfigSource;
import org.jboss.resteasy.microprofile.config.ServletContextConfigSource;
import org.jboss.resteasy.microprofile.test.config.resource.MicroProfileConfigFilter;
import org.jboss.resteasy.microprofile.test.config.resource.MicroProfileConfigResource;
import org.jboss.resteasy.microprofile.test.config.resource.TestConfigApplication;
import org.jboss.resteasy.microprofile.test.util.PermissionUtil;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @tpSubChapter MicroProfile Config
 * @tpChapter Integration tests
 * @tpSince RESTEasy 4.6.0
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ConfigSourceOverrideOrdinalServletContextListenerTest {

    static Client client;

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> deploy() {
        return TestEnvironment.createWar(ConfigSourceOverrideOrdinalServletContextListenerTest.class)
                .addClasses(TestConfigApplication.class, MicroProfileConfigFilter.class, MicroProfileConfigResource.class)
                .setWebXML(ConfigSourceOverrideOrdinalServletContextListenerTest.class.getPackage(), "web_override_ordinal_servlet_context_listener.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(PermissionUtil.createPermissionsXmlAsset(
                        new PropertyPermission("system", "write")
                ), "permissions.xml");
    }

    @BeforeClass
    public static void before() throws Exception {
        client = ClientBuilder.newClient();
    }

    @AfterClass
    public static void after() throws Exception {
        client.close();
    }

    private URI generateURL(String path) throws URISyntaxException {
        return TestEnvironment.generateUri(url, path);
    }

    /**
     * @tpTestDetails Verify all built in ConfigSources ordinal for Config retrieved programmatically.
     * @tpSince RESTEasy 4.6.0
     */
    @Test
    public void testBuiltInConfigSourcesOrdinalProgrammatically() throws Exception {
        Map<String, Integer> builtInConfigSourceOrdinals = client.target(generateURL("/configSources/ordinal"))
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<Map<String, Integer>>() {
                });

        checkBuiltInConfigSourcesOrdinal(builtInConfigSourceOrdinals);
    }

    /**
     * @tpTestDetails Verify all built in ConfigSources ordinal for Config retrieved by injection.
     * @tpSince RESTEasy 4.6.0
     */
    @Test
    public void testBuiltInConfigSourcesOrdinalInjected() throws Exception {
        Map<String, Integer> builtInConfigSourceOrdinals = client.target(generateURL("/configSources/ordinal"))
                .queryParam("inject", Boolean.TRUE)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<Map<String, Integer>>() {
                });

        checkBuiltInConfigSourcesOrdinal(builtInConfigSourceOrdinals);
    }

    private void checkBuiltInConfigSourcesOrdinal(Map<String, Integer> builtInConfigSourcesOrdinal) {
        Integer servletConfigSourceDefaultOrdinal = 30;
        Integer servletContextConfigSourceDefaultOrdinal = 10;

        Assert.assertEquals(servletConfigSourceDefaultOrdinal, builtInConfigSourcesOrdinal.get(ServletConfigSource.class
                .getName()));
        Assert.assertEquals(servletContextConfigSourceDefaultOrdinal, builtInConfigSourcesOrdinal.get(ServletContextConfigSource.class
                .getName()));
    }

}
