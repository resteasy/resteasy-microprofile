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

import java.net.URISyntaxException;
import java.net.URL;

import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.HealthCheckData;
import org.jboss.resteasy.microprofile.test.client.integration.resource.HealthService;
import org.jboss.resteasy.microprofile.test.client.integration.resource.Ignore404ExceptionMapper;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @tpSubChapter MicroProfile rest client
 * @tpChapter Integration tests
 * @tpTestCaseDetails The microprofile-rest-client 2.0 specification describes use of a custom
 *                    ResponseExceptionMapper implementation and a default ResponseExceptionMapper.
 *                    There is a scenario in which the default ResponseExceptionMapper has been disabled
 *                    and the custom ResponseExceptionMapper returns null. This test verifies the proper
 *                    behavior for this scenario and when the default ResponseExceptionMapper is present.
 * @tpSince RESTEasy 4.7.0
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class ExceptionMapperReturnsNullTest {

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> deploy() {
        return TestEnvironment.createWar(ExceptionMapperReturnsNullTest.class);
    }

    @Test
    public void testNoApplicableExceptionMapper() throws Exception {
        try (HealthService healthServiceClient = getHealthServiceBuilder()
                .property("microprofile.rest.client.disable.default.mapper", true)
                .build(HealthService.class)) {
            Assertions.assertNull(healthServiceClient.getHealthData());
        }
    }

    @Test
    public void testGlobalNoApplicableExceptionMapper() throws Exception {
        System.setProperty("microprofile.rest.client.disable.default.mapper", "true");
        try (HealthService healthServiceClient = getHealthServiceBuilder()
                .build(HealthService.class)) {
            HealthCheckData data = healthServiceClient.getHealthData();
            Assertions.assertNull(data);
        } finally {
            System.clearProperty("microprofile.rest.client.disable.default.mapper");
        }
    }

    @Test
    public void testAnnotationNoApplicableExceptionMapper() throws Exception {
        System.setProperty("HealthService/mp-rest/disableDefaultMapper", "true");
        try (HealthService healthServiceClient = getHealthServiceBuilder()
                .build(HealthService.class)) {
            HealthCheckData data = healthServiceClient.getHealthData();
            Assertions.assertNull(data);
        } finally {
            System.clearProperty("HealthService/mp-rest/disableDefaultMapper");
        }
    }

    @Test
    public void testDefaultExceptionMapper() throws Exception {
        try (HealthService healthServiceClient = getHealthServiceBuilder()
                .build(HealthService.class)) {
            Assertions.assertThrows(WebApplicationException.class, healthServiceClient::getHealthData);
        }
    }

    private RestClientBuilder getHealthServiceBuilder() throws URISyntaxException {
        return RestClientBuilder.newBuilder()
                .baseUri(TestEnvironment.generateUri(url, "test-app"))
                .register(new Ignore404ExceptionMapper());
    }
}
