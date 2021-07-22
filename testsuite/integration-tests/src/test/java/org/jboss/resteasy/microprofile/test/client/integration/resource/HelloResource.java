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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.reactivex.Single;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.Assert;

@Path("/")
public class HelloResource {

    @Inject
    @RestClient
    HelloClient rest;

    @GET
    @Produces("text/plain")
    @Path("/hello")
    public String hello() {
        return "Hello";
    }

    @GET
    @Produces("text/plain")
    @Path("/null-path-param/{value}")
    public String nullPathParam(@PathParam("value") final String value) {
        return value;
    }

    @GET
    @Path("/null-query-param/")
    public String nullQueryParam(@QueryParam("value") String value) {
        return value;
    }

    @GET
    @Path("/some/{id}")
    public Single<String> single(@PathParam("id") String id) {
        return Single.just(id);
    }

    @GET
    @Path("/cs/{id}")
    public CompletionStage<String> cs(@PathParam("id") String id) {
        return CompletableFuture.completedFuture(id);
    }

    @GET
    @Path("async-client-target")
    public CompletionStage<String> asyncClientTarget(@HeaderParam("X-Propagated") String propagatedHeader,
                                                     @HeaderParam("X-Not-Propagated") String nonPropagatedHeader) {
        Assert.assertNull(nonPropagatedHeader);
        Assert.assertEquals("got-a-value", propagatedHeader);
        return CompletableFuture.completedFuture("OK");
    }

    @GET
    @Path("async-client")
    public CompletionStage<String> asyncClient(@HeaderParam("X-Propagated") String propagatedHeader,
                                               @HeaderParam("X-Not-Propagated") String nonPropagatedHeader) {
        Assert.assertEquals("got-a-value", propagatedHeader);
        Assert.assertEquals("got-a-value", nonPropagatedHeader);
        return rest.asyncClientTarget();
    }

    @GET
    @Path("client-target")
    public String clientTarget(@HeaderParam("X-Propagated") String propagatedHeader,
                               @HeaderParam("X-Not-Propagated") String nonPropagatedHeader) {
        Assert.assertNull(nonPropagatedHeader);
        Assert.assertEquals("got-a-value", propagatedHeader);
        return "OK";
    }

    @GET
    @Path("client")
    public String client(@HeaderParam("X-Propagated") String propagatedHeader,
                         @HeaderParam("X-Not-Propagated") String nonPropagatedHeader) {
        Assert.assertEquals("got-a-value", propagatedHeader);
        Assert.assertEquals("got-a-value", nonPropagatedHeader);
        return rest.clientTarget();
    }

    @GET
    @Path("async-client-404")
    public CompletionStage<String> asyncClient404() {
        return rest.asyncClient404Target();
    }
}