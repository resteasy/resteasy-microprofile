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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ClientHeaderParam(name = "test-global-header", value = "GlobalSubResourceHeader")
public interface ClientSubResource {

    @GET
    Response fromSub() throws TestException;

    @GET
    @ClientHeaderParam(name = "test-header", value = "SubResourceHeader")
    @Path("/header")
    @Produces(MediaType.TEXT_PLAIN)
    Response withHeader();

    @GET
    @Path("/global/header")
    @Produces(MediaType.TEXT_PLAIN)
    Response withGlobalHeader();
}
