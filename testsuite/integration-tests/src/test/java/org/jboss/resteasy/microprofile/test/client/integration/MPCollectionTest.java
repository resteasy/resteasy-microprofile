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
import java.util.List;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MPCollectionActivator;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MPCollectionService;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MPCollectionServiceIntf;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @tpSubChapter MicroProfile Config
 * @tpChapter Integration tests
 * @tpTestCaseDetails Show how to get the proxy for the service.
 * @tpSince RESTEasy 4.6.0
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class MPCollectionTest {

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> serviceDeploy() {
        return TestEnvironment.createWar(MPCollectionTest.class)
                .addClasses(MPCollectionService.class, MPCollectionActivator.class);
    }

    @Test
    public void testOne() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        // uri is http://localhost:port/{context-root}
        MPCollectionServiceIntf mpc = builder.baseUri(TestEnvironment.generateUri(url))
                .build(MPCollectionServiceIntf.class);
        List<String> l = mpc.getList();
        Assertions.assertEquals(3, l.size());
    }
}
