package org.jboss.resteasy.microprofile.test.client.integration.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface NgHTTP2 {
    @GET
    @Path("httpbin/get")
    String get();
}
