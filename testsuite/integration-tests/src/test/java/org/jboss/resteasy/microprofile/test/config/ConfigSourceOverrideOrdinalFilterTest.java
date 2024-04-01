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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.resteasy.microprofile.config.FilterConfigSource;
import org.jboss.resteasy.microprofile.config.ServletContextConfigSource;
import org.jboss.resteasy.microprofile.test.config.resource.MicroProfileConfigFilter;
import org.jboss.resteasy.microprofile.test.config.resource.MicroProfileConfigResource;
import org.jboss.resteasy.microprofile.test.config.resource.TestConfigApplication;
import org.jboss.resteasy.microprofile.test.util.MicroProfileConfigSystemPropertySetupTask;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @tpSubChapter MicroProfile Config
 * @tpChapter Integration tests
 * @tpSince RESTEasy 4.6.0
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
@ServerSetup(MicroProfileConfigSystemPropertySetupTask.class)
public class ConfigSourceOverrideOrdinalFilterTest {

    static Client client;

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> deploy() {
        return TestEnvironment.createWar(ConfigSourceOverrideOrdinalFilterTest.class)
                .addClasses(TestConfigApplication.class, MicroProfileConfigFilter.class, MicroProfileConfigResource.class)
                .setWebXML(ConfigSourceOverrideOrdinalFilterTest.class.getPackage(), "web_override_ordinal_filter.xml")
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
        Integer filterConfigSourceDefaultOrdinal = 20;
        Integer servletContextConfigSourceDefaultOrdinal = 10;

        Assertions.assertEquals(filterConfigSourceDefaultOrdinal,
                builtInConfigSourcesOrdinal.get(FilterConfigSource.class.getName()));
        Assertions.assertEquals(servletContextConfigSourceDefaultOrdinal,
                builtInConfigSourcesOrdinal.get(ServletContextConfigSource.class
                        .getName()));
    }

}
