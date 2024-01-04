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

package org.jboss.resteasy.microprofile.test.client.exception.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Path("/root")
public class ServerResource {

    @GET
    public Response fromRoot() {
        return Response.serverError().entity("RootResource failed on purpose").build();
    }

    @GET
    @Path("/sub")
    public Response fromSub() {
        return Response.serverError().entity("SubResource failed on purpose").build();
    }

    @GET
    @Path("/sub/header")
    @Produces(MediaType.TEXT_PLAIN)
    public Response subHeader(@HeaderParam("test-header") final String value) {
        return Response.ok(value).build();
    }

    @GET
    @Path("/sub/global/header")
    @Produces(MediaType.TEXT_PLAIN)
    public Response subGlobalHeader(@HeaderParam("test-global-header") final String value) {
        return Response.ok(value).build();
    }
}
