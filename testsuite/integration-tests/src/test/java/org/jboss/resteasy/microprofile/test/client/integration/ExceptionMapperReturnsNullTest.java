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

import java.net.URL;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.HealthCheckData;
import org.jboss.resteasy.microprofile.test.client.integration.resource.HealthService;
import org.jboss.resteasy.microprofile.test.client.integration.resource.Ignore404ExceptionMapper;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

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
@RunWith(Arquillian.class)
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
        HealthCheckData data = RestClientBuilder.newBuilder()
                .property("microprofile.rest.client.disable.default.mapper", true)
                .baseUri(TestEnvironment.generateUri(url, "test-app"))
                .register(new Ignore404ExceptionMapper())
                .build(HealthService.class)
                .getHealthData();
        Assert.assertNull(data);
    }

    @Test
    public void testDefaultExceptionMapper() {
        try {
            RestClientBuilder.newBuilder()
                    .baseUri(TestEnvironment.generateUri(url, "test-app"))
                    .register(new Ignore404ExceptionMapper())
                    .build(HealthService.class)
                    .getHealthData();
            Assert.fail("Exception should have been returned");
        } catch (Exception e) {
            // success exception was returned
        }
    }
}
