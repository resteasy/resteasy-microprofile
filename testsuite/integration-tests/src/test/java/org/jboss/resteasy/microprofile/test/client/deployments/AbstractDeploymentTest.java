/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2025 Red Hat, Inc., and individual contributors
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

package org.jboss.resteasy.microprofile.test.client.deployments;

import java.net.URI;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.jboss.resteasy.microprofile.test.client.deployments.model.Message;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.junit.jupiter.api.Assertions;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
abstract class AbstractDeploymentTest {

    protected UriBuilder uriBuilder(final String serviceName) {
        return UriBuilder.fromUri(TestEnvironment.getHttpUrl()).path(serviceName).path("test-app/client");
    }

    Message readMessage(final Client client, final URI uri) {
        try (Response response = client.target(uri).request().get()) {
            Assertions.assertEquals(200, response.getStatus());
            final Message message = response.readEntity(Message.class);
            Assertions.assertNotNull(message);
            return message;
        }
    }
}
