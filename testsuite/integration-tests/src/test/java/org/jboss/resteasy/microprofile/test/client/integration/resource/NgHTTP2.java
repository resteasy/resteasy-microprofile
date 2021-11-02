package org.jboss.resteasy.microprofile.test.client.integration.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface NgHTTP2 {
    @GET
    @Path("httpbin/get")
    String get();
}
