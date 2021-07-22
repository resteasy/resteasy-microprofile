package org.jboss.resteasy.microprofile.test.client.integration.resource;

import java.io.Closeable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

public interface HealthService extends Closeable {
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/health")
    HealthCheckData getHealthData() throws WebApplicationException;
}