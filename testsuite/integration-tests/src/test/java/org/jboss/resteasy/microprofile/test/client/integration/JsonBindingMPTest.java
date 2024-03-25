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
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.Dog;
import org.jboss.resteasy.microprofile.test.client.integration.resource.JsonBindingMPService;
import org.jboss.resteasy.microprofile.test.client.integration.resource.JsonBindingMPServiceIntf;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @tpSubChapter MicroProfile rest client
 * @tpChapter Integration tests
 * @tpTestCaseDetails Show JSON-Binding is supported.
 * @tpSince RESTEasy 4.6.0
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class JsonBindingMPTest {

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> serviceDeploy() {
        return TestEnvironment.createWar(JsonBindingMPTest.class)
                .addClasses(JsonBindingMPService.class, Dog.class);
    }

    @Test
    public void testDog() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        JsonBindingMPServiceIntf jsonBindingMPServiceIntf = builder
                .baseUri(TestEnvironment.generateUri(url, "test-app"))
                .build(JsonBindingMPServiceIntf.class);

        try {
            Dog dog = new Dog("Rex", "german shepherd");
            Dog response = jsonBindingMPServiceIntf.getDog(dog);
            Assertions.assertEquals("Jethro", response.getName());
            Assertions.assertEquals("stafford", response.getSort());
        } catch (Exception e) {
            Assertions.fail("Exception thrown: " + e);
        }
    }
}
