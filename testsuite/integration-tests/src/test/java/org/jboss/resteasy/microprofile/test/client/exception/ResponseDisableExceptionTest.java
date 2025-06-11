/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
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

package org.jboss.resteasy.microprofile.test.client.exception;

import java.net.URL;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ProxyExceptionFactory;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ProxyExceptionResource;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class ResponseDisableExceptionTest {

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> deploySimpleResource() {
        return TestEnvironment.createWar(ResponseDisableExceptionTest.class)
                .addClasses(ProxyExceptionResource.class);
    }

    /**
     * response and exception mapping
     */
    @Test
    public void testResponseExceptions() throws Exception {
        System.setProperty("microprofile.rest.client.disable.response.exceptions", "true");
        try {
            ProxyExceptionFactory mpRestClient = RestClientBuilder.newBuilder()
                    .baseUri(TestEnvironment.generateUri(url, "test-app"))
                    .build(ProxyExceptionFactory.class);
            Assertions.assertThrows(BadRequestException.class,
                    () -> mpRestClient.asException(400, "Bad Parameter"));
            Response response = mpRestClient.asResponse(400, "Bad Parameter");
            Assertions.assertEquals(400, response.getStatusInfo().getStatusCode());
        } finally {
            System.clearProperty("microprofile.rest.client.disable.response.exceptions");
        }
    }
}
