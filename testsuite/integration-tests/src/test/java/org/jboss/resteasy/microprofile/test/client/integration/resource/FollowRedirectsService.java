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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/theService")
public class FollowRedirectsService {
    private static final String prefix = "/thePatron";

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("get")
    public List<String> getList() {
        List<String> l = new ArrayList<>();
        l.add("theService reached");
        return l;
    }

    @GET
    @Path("tmpRedirect/{p}/{testname}")
    public Response tmpRedirect(@PathParam("p") String p,
                                @PathParam("testname") String testname) {
        return Response.temporaryRedirect(
                createUri("/" + p + "/redirected", testname))
                .build();
    }

    @Path("post-redirect")
    @POST
    public Response postRedirect(String testname) {
        return Response.seeOther(
                createUri(prefix + "/redirected", testname))
                .build();
    }

    @GET
    @Path("movedPermanently/{p}/{testname}")
    public Response movedPermanently(@PathParam("p") String p,
                                     @PathParam("testname") String testname) {
        return Response.status(301).header("location",
                createUri("/" + p + "/redirectedDirectResponse", testname))
                .build();
    }


    @GET
    @Path("found/{p}/{testname}")
    public Response found(@PathParam("p") String p,
                          @PathParam("testname") String testname) {
        return Response.status(302).header("location",
                createUri("/" + p + "/redirectedDirectResponse", testname))
                .build();
    }

    @GET
    @Path("ping")
    public String ping() {
        return "pong";
    }

    @GET
    @Path("redirect/ping")
    public Response redirectPing() {
        return Response.temporaryRedirect(
                URI.create(uriInfo.getBaseUri()  + uriInfo.getPathSegments().get(0).getPath() + "/ping"))
                .build();
    }

    private URI createUri(final String path, final String testName) {
        final URI base = uriInfo.getBaseUri();
        final StringBuilder builder = new StringBuilder();
        builder.append(base.getScheme())
                .append("://")
                .append(base.getHost())
                .append(':')
                .append(base.getPort());
        if (testName.length() > 0) {
            if (testName.charAt(0) == '/') {
                builder.append(testName);
            } else {
                builder.append('/').append(testName);
            }
        }
        // The REST context path
        builder.append("/test-app");
        if (path.length() > 0) {
            if (path.charAt(0) == '/') {
                builder.append(path);
            } else {
                builder.append('/').append(path);
            }
        }

        return URI.create(builder.toString());
    }
}
