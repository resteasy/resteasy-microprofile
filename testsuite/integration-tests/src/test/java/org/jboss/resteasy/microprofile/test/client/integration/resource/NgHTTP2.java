package org.jboss.resteasy.microprofile.test.client.integration.resource;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@RegisterRestClient
public interface NgHTTP2 {
    @GET
    @Path("httpbin/get")
    String get();
}
