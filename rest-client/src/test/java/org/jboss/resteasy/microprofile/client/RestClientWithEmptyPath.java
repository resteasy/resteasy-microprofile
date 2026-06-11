package org.jboss.resteasy.microprofile.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("")
public interface RestClientWithEmptyPath {
    @Path("/{aPathParameter}")
    @GET
    String get(@PathParam("aPathParameter") String aPathParameter);
}