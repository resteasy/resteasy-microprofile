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

import java.io.IOException;
import java.util.Map;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.microprofile.test.client.deployments.model.Message;
import org.jboss.resteasy.microprofile.test.client.deployments.resource.MessageClient;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
abstract class AbstractMultipleDeploymentsTest extends AbstractDeploymentTest {
    static final String DEPLOYMENT_1 = "deployment1";
    static final String DEPLOYMENT_2 = "deployment2";

    static JavaArchive createSharedJar() {
        return ShrinkWrap.create(JavaArchive.class, "shared.jar")
                .addClass(Message.class);
    }

    static WebArchive createWar(final String deploymentName, final String serviceName) throws IOException {
        final Map<String, String> config = Map.of(MessageClient.class.getName() + "/mp-rest/uri",
                TestEnvironment.getHttpUrl() + serviceName + "/test-app", "dev.resteasy.test.deployment.name", deploymentName);
        return TestEnvironment
                .createWar(deploymentName, config)
                .addPackage(MessageClient.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @Order(1)
    public void sendDeployment1Message() {
        try (Client client = ClientBuilder.newClient()) {
            final Message message = new Message();
            message.setText("Hello World");
            try (
                    Response response = client.target(uriBuilder(DEPLOYMENT_1))
                            .request()
                            .post(Entity.json(message))) {
                Assertions.assertEquals(201, response.getStatus());
                final Message foundMessage = readMessage(client, response.getLocation());
                Assertions.assertEquals("Hello World", foundMessage.getText());
                Assertions.assertEquals(DEPLOYMENT_2, foundMessage.getTarget(),
                        "Expected the request to be sent to " + DEPLOYMENT_2);
            }
        }
    }

    @Test
    @Order(2)
    public void getDeployment1Message() {
        try (Client client = ClientBuilder.newClient()) {
            try (
                    Response response = client.target(uriBuilder(DEPLOYMENT_1).path("1"))
                            .request()
                            .get()) {
                Assertions.assertEquals(200, response.getStatus());
                final Message foundMessage = response.readEntity(Message.class);
                Assertions.assertEquals("Hello World", foundMessage.getText());
                Assertions.assertEquals(DEPLOYMENT_2, foundMessage.getTarget(),
                        "Expected the request to be sent to " + DEPLOYMENT_2);
            }
        }
    }

    @Test
    @Order(3)
    public void deleteDeployment1Message() {
        try (Client client = ClientBuilder.newClient()) {
            try (
                    Response response = client.target(uriBuilder(DEPLOYMENT_1).path("1"))
                            .request()
                            .delete()) {
                Assertions.assertEquals(200, response.getStatus());
                final Message foundMessage = response.readEntity(Message.class);
                Assertions.assertEquals("Hello World", foundMessage.getText());
                Assertions.assertEquals(DEPLOYMENT_2, foundMessage.getTarget(),
                        "Expected the request to be sent to " + DEPLOYMENT_2);
            }
        }
    }

    @Test
    @Order(4)
    public void sendDeployment2Message() {
        try (Client client = ClientBuilder.newClient()) {
            final Message message = new Message();
            message.setText("Hello World");
            try (
                    Response response = client.target(uriBuilder(DEPLOYMENT_2))
                            .request()
                            .post(Entity.json(message))) {
                Assertions.assertEquals(201, response.getStatus());
                final Message foundMessage = readMessage(client, response.getLocation());
                Assertions.assertEquals("Hello World", foundMessage.getText());
                Assertions.assertEquals(DEPLOYMENT_1, foundMessage.getTarget(),
                        "Expected the request to be sent to " + DEPLOYMENT_1);
            }
        }
    }

    @Test
    @Order(4)
    public void getDeployment2Message() {
        try (Client client = ClientBuilder.newClient()) {
            try (
                    Response response = client.target(uriBuilder(DEPLOYMENT_2).path("1"))
                            .request()
                            .get()) {
                Assertions.assertEquals(200, response.getStatus());
                final Message foundMessage = response.readEntity(Message.class);
                Assertions.assertEquals("Hello World", foundMessage.getText());
                Assertions.assertEquals(DEPLOYMENT_1, foundMessage.getTarget(),
                        "Expected the request to be sent to " + DEPLOYMENT_1);
            }
        }
    }

    @Test
    @Order(5)
    public void deleteDeployment2Message() {
        try (Client client = ClientBuilder.newClient()) {
            try (
                    Response response = client.target(uriBuilder(DEPLOYMENT_2).path("1"))
                            .request()
                            .delete()) {
                Assertions.assertEquals(200, response.getStatus());
                final Message foundMessage = response.readEntity(Message.class);
                Assertions.assertEquals("Hello World", foundMessage.getText());
                Assertions.assertEquals(DEPLOYMENT_1, foundMessage.getTarget(),
                        "Expected the request to be sent to " + DEPLOYMENT_1);
            }
        }
    }
}
