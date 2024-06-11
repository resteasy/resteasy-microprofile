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

package org.jboss.resteasy.microprofile.test.client.integration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MultiPartClient;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MultiPartResource;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;

/**
 * Tests the {@link EntityPart} arguments work with an MP REST Client.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class MultiPartClientTest {

    @ArquillianResource
    private URI uri;

    @Deployment
    public static Archive<?> serviceDeploy() {
        return TestEnvironment.createWar(MultiPartClientTest.class)
                .addClasses(MultiPartResource.class)
                .addAsManifestResource(DeploymentDescriptors.createPermissionsXmlAsset(
                        DeploymentDescriptors.createTempDirPermission("read,write"),
                        new PropertyPermission("java.io.tmpdir", "read")), "permissions.xml");
    }

    @Test
    public void uploadFile(final TestInfo testInfo) throws Exception {
        final String methodName = testInfo.getTestMethod().orElseThrow().getName();
        try (MultiPartClient client = RestClientBuilder.newBuilder().baseUri(uri).build(MultiPartClient.class)) {
            final byte[] content;
            try (InputStream in = MultiPartClientTest.class.getResourceAsStream("/multipart/test-file1.txt")) {
                Assertions.assertNotNull(in, "Could not find /multipart/test-file1.txt");
                content = in.readAllBytes();
            }
            // Send in an InputStream to ensure it works with an InputStream
            final List<EntityPart> files = List.of(EntityPart.withFileName("test-file1.txt")
                    .content(new ByteArrayInputStream(content))
                    .mediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                    .build());
            client.uploadFile(methodName, files);
            final List<EntityPart> downloads = client.download(methodName);
            Assertions.assertNotNull(downloads);
            Assertions.assertEquals(1, downloads.size(), () -> "Expected 1 entity but got " + downloads);
            final EntityPart downloadedFile = downloads.get(0);
            Assertions.assertEquals("test-file1.txt", downloadedFile.getName());
            Assertions.assertArrayEquals(content, downloadedFile.getContent().readAllBytes());
        }
    }

    @Test
    public void uploadMultipleFiles(final TestInfo testInfo) throws Exception {
        final String methodName = testInfo.getTestMethod().orElseThrow().getName();
        try (MultiPartClient client = RestClientBuilder.newBuilder().baseUri(uri).build(MultiPartClient.class)) {
            final Map<String, byte[]> entityPartContent = new LinkedHashMap<>(2);
            try (InputStream in = MultiPartClientTest.class.getResourceAsStream("/multipart/test-file1.txt")) {
                Assertions.assertNotNull(in, "Could not find /multipart/test-file1.txt");
                entityPartContent.put("test-file1.txt", in.readAllBytes());
            }
            try (InputStream in = MultiPartClientTest.class.getResourceAsStream("/multipart/test-file2.txt")) {
                Assertions.assertNotNull(in, "Could not find /multipart/test-file2.txt");
                entityPartContent.put("test-file2.txt", in.readAllBytes());
            }
            final List<EntityPart> files = entityPartContent.entrySet()
                    .stream()
                    .map((entry) -> {
                        try {
                            return EntityPart.withName(entry.getKey())
                                    .fileName(entry.getKey())
                                    .content(entry.getValue())
                                    .mediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                                    .build();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .collect(Collectors.toList());
            client.uploadFile(methodName, files);
            final List<EntityPart> downloads = client.download(methodName);
            Assertions.assertNotNull(downloads);
            Assertions.assertEquals(2, downloads.size(),
                    () -> "Expected 2 entity but got " + System.lineSeparator() + toString(downloads));
        }
    }

    private static String toString(final Collection<EntityPart> parts) {
        final StringBuilder builder = new StringBuilder();
        try {
            for (EntityPart part : parts) {
                builder.append("name=").append(part.getName())
                        .append(", fileName=").append(part.getFileName().orElse(null))
                        .append(", content=").append(part.getContent(String.class))
                        .append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return builder.toString();
    }
}
