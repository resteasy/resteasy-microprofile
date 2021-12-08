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

package org.jboss.resteasy.microprofile.test.client.param.resource;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.jboss.resteasy.annotations.jaxrs.CookieParam;
import org.jboss.resteasy.annotations.jaxrs.FormParam;
import org.jboss.resteasy.annotations.jaxrs.HeaderParam;
import org.jboss.resteasy.annotations.jaxrs.MatrixParam;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

/**
 * Created by Marek Marusic <mmarusic@redhat.com> on 1/16/19.
 */
@Path("/")
public class ProxyParameterAnotationsResource {

    @Path("AllParams/{pathParam}")
    @POST
    public String executeAllParams(@QueryParam String queryParam,
            @HeaderParam String headerParam,
            @CookieParam String cookieParam,
            @PathParam("pathParam") String pathParam,
            @FormParam String formParam,
            @MatrixParam String matrixParam) {
        return queryParam + " " + headerParam + " " + cookieParam + " " + pathParam + " " + formParam + " " + matrixParam;
    }
}
