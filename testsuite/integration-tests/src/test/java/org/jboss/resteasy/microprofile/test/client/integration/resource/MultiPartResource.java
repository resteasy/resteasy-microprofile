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

package org.jboss.resteasy.microprofile.test.client.integration.resource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.MULTIPART_FORM_DATA)
@ApplicationScoped
@Path("/")
public class MultiPartResource {
    private final java.nio.file.Path dir;

    public MultiPartResource() {
        dir = Paths.get(System.getProperty("java.io.tmpdir"), "multipart");
    }

    @POST
    @Path("upload/{testName}")
    public void uploadFiles(@PathParam("testName") final String testName, final List<EntityPart> entityParts)
            throws IOException {
        final var path = dir.resolve(testName);
        Files.createDirectories(path);
        for (EntityPart part : entityParts) {
            if (part.getFileName().isPresent()) {
                Files.copy(part.getContent(), path.resolve(part.getFileName().get()));
            } else {
                throw new BadRequestException("No file name for entity part " + part);
            }
        }
    }

    @GET
    @Path("/download/{testName}")
    public List<EntityPart> download(@PathParam("testName") final String testName) throws IOException {
        final var path = dir.resolve(testName);
        if (Files.notExists(path)) {
            throw new NotFoundException("Could not find download path " + testName);
        }
        try (var paths = Files.walk(path)) {
            return paths.filter(Files::isRegularFile)
                    .map((file) -> {
                        try {
                            return EntityPart.withName(file.getFileName().toString())
                                    .fileName(file.getFileName().toString())
                                    .content(Files.newInputStream(file))
                                    .mediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                                    .build();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }).collect(Collectors.toList());
        }
    }
}
