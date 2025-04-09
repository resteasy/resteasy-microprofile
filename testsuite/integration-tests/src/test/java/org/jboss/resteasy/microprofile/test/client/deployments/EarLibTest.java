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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.resteasy.microprofile.test.client.deployments.model.Message;
import org.jboss.resteasy.microprofile.test.client.deployments.resource.ClientMessageResource;
import org.jboss.resteasy.microprofile.test.client.deployments.resource.MessageClient;
import org.jboss.resteasy.microprofile.test.client.deployments.resource.MessageResource;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ArquillianTest
@RunAsClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EarLibTest extends AbstractDeploymentTest {
    private static final String WAR_DEPLOYMENT = "inside-ear-war";
    private static final String TARGET_NAME = "inside-war";

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() throws IOException {
        final Map<String, String> earConfig = Map.of(MessageClient.class.getName() + "/mp-rest/uri",
                TestEnvironment.getHttpUrl() + WAR_DEPLOYMENT + "/test-app", "dev.resteasy.test.deployment.name",
                "ear");
        final Map<String, String> warConfig = Map.of(MessageClient.class.getName() + "/mp-rest/uri",
                TestEnvironment.getHttpUrl() + WAR_DEPLOYMENT + "/test-app", "dev.resteasy.test.deployment.name",
                TARGET_NAME);
        return TestEnvironment.addConfigProperties(ShrinkWrap.create(EnterpriseArchive.class, "lib-in-ear.ear")
                .addAsLibrary(ShrinkWrap.create(JavaArchive.class, "shared.jar")
                        .addClasses(Message.class, MessageClient.class))
                .addAsModule(TestEnvironment
                        .createWar(WAR_DEPLOYMENT, warConfig)
                        .addClasses(ClientMessageResource.class, MessageResource.class)
                        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")),
                earConfig);
    }

    @Test
    @Order(1)
    public void sendMessage() {
        try (Client client = ClientBuilder.newClient()) {
            final Message message = new Message();
            message.setText("Hello World");
            try (
                    Response response = client.target(uriBuilder(WAR_DEPLOYMENT))
                            .request()
                            .post(Entity.json(message))) {
                Assertions.assertEquals(201, response.getStatus());
                final Message foundMessage = readMessage(client, response.getLocation());
                Assertions.assertEquals("Hello World", foundMessage.getText());
                Assertions.assertEquals(TARGET_NAME, foundMessage.getTarget(),
                        "Expected the request to be sent to " + TARGET_NAME);
            }
        }
    }

    @Test
    @Order(2)
    public void getMessage() {
        try (Client client = ClientBuilder.newClient()) {
            try (
                    Response response = client.target(uriBuilder(WAR_DEPLOYMENT).path("1"))
                            .request()
                            .get()) {
                Assertions.assertEquals(200, response.getStatus());
                final Message foundMessage = response.readEntity(Message.class);
                Assertions.assertEquals("Hello World", foundMessage.getText());
                Assertions.assertEquals(TARGET_NAME, foundMessage.getTarget(),
                        "Expected the request to be sent to " + TARGET_NAME);
            }
        }
    }

    @Test
    @Order(3)
    public void deleteMessage() {
        try (Client client = ClientBuilder.newClient()) {
            try (
                    Response response = client.target(uriBuilder(WAR_DEPLOYMENT).path("1"))
                            .request()
                            .delete()) {
                Assertions.assertEquals(200, response.getStatus());
                final Message foundMessage = response.readEntity(Message.class);
                Assertions.assertEquals("Hello World", foundMessage.getText());
                Assertions.assertEquals(TARGET_NAME, foundMessage.getTarget(),
                        "Expected the request to be sent to " + TARGET_NAME);
            }
        }
    }
}
