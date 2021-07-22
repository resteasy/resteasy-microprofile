package org.jboss.resteasy.microprofile.test.client.param.resource;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

/**
 * Created by Marek Marusic <mmarusic@redhat.com> on 3/4/19.
 */
@Path("/a")
@RegisterRestClient
public interface ProxyBeanParam {

    @Path("a/{p1}/{p2}/{p3}")
    @GET
    String getAll(@BeanParam Params beanParam, @PathParam("p2") String p2, @QueryParam String queryParam);
}
