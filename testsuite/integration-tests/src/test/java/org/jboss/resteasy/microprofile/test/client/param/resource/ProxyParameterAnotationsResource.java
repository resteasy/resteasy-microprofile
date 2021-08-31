package org.jboss.resteasy.microprofile.test.client.param.resource;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

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
