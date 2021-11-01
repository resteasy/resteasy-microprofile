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

package org.jboss.resteasy.microprofile.test.client.integration.resource;

import java.util.concurrent.CompletionStage;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@Produces("text/plain")
@RegisterRestClient
@RegisterClientHeaders(HeaderPropagator.class)
public interface HelloClient {

    @GET
    @Path("/hello")
    String hello();

    @GET
    @Path("/null-path-param/{value}")
    String nullPathParam(@PathParam("value") String value);

    @GET
    @Path("/null-query-param/")
    String nullQueryParam(@QueryParam("value") String value);

    @GET
    @Path("some/{id}")
    CompletionStage<String> some(@PathParam("id") String id);

    @GET
    @Path("cs/{id}")
    CompletionStage<String> cs(@PathParam("id") String id);

    @GET
    @Path("async-client-target")
    CompletionStage<String> asyncClientTarget();

    @GET
    @Path("async-client")
    @ClientHeaderParam(name = "X-Propagated", value = "got-a-value")
    @ClientHeaderParam(name = "X-Not-Propagated", value = "got-a-value")
    CompletionStage<String> asyncClient();

    @GET
    @Path("client-target")
    String clientTarget();

    @GET
    @Path("client")
    @ClientHeaderParam(name = "X-Propagated", value = "got-a-value")
    @ClientHeaderParam(name = "X-Not-Propagated", value = "got-a-value")
    String client();

    @GET
    @Path("async-client-404")
    CompletionStage<String> asyncClient404();

    @GET
    @Path("async-client-404-target")
    CompletionStage<String> asyncClient404Target();
}