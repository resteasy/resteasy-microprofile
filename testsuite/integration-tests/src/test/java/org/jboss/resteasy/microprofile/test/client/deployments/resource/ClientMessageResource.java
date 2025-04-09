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

package org.jboss.resteasy.microprofile.test.client.deployments.resource;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.microprofile.test.client.deployments.model.Message;

/**
 * This is a simple endpoint to invoke injected client calls.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Path("/client")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class ClientMessageResource {

    @Inject
    @RestClient
    private MessageClient messageClient;

    @GET
    public List<Message> messages() {
        return messageClient.messages();
    }

    @GET
    @Path("{id}")
    public Message getUser(@PathParam("id") final int id) {
        return messageClient.get(id);
    }

    @POST
    public Response push(final Message user) {
        return messageClient.push(user);
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") final int id) {
        return messageClient.delete(id);
    }
}
